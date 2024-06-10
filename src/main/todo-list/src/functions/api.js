import axios from "axios";
import { logout } from "./auth";

let isRefreshing = false;
let refreshSubcribers = [];

// 새로운 Axios 인스턴스 생성
const api = axios.create({
  baseURL: '/',
  Authorization: 'Bearer ' + sessionStorage.getItem("accessToken")
})

api.interceptors.request.use(
  config => {
    if (!config.headers.Authorization) {
      config.headers.Authorization = 'Bearer ' + sessionStorage.getItem("accessToken");
    }
    return config
  },

  err => {
    return err
  }
)

// Axios 인스턴스에 응답 인터셉터 추가
api.interceptors.response.use(
  // 응답이 성공하면 응답 그대로 반환
  res => {
    return res;
  },

  // 에러가 발생한 경우 (AccessToken 만료를 처리하기 위함)
  err => {
    const {
      config,
      response: {status}
    } = err;
    const originalRequest = config;

    // ._retry : 요청이 재시도 됐는지 여부
    if (status === 401 && !originalRequest._retry) {
      
      // 토큰 갱신 요청을 안한 경우 => 요청해야 함
      if (!isRefreshing) {
        isRefreshing = true;
        originalRequest._retry = true;

        return new Promise((resolve, reject) => {
          axios.get('/api/v1/user/refresh')
            .then(({ data }) => {
              api.defaults.headers.common['Authorization'] = 'Bearer ' + data.body;
              originalRequest.headers['Authorization'] = 'Bearer ' + data.body;
              sessionStorage.setItem('accessToken', data.body)

              processQueue(null, data.body); // queue에 쌓인 콜백 처리
              resolve(api(originalRequest)); // 토큰 갱신 후 요청 재시도
            })
            .catch((err) => {
              if (err.response.status === 401) {
                logout("세션이 만료되어 ");
                return;
              }
              processQueue(err, null);
              reject(err);
            })
            .finally(() => {
              isRefreshing = false;
            });
        });
      }

      // 토큰 갱신 요청이 진행 중일 경우
      return new Promise((resolve, reject) => {
        subscribeTokenRefresh((token) => {
          originalRequest.headers['Authorization'] = 'Bearer ' + token;
          resolve(api(originalRequest)); // 토큰 갱신 후 요청 재시도
        }, (err) => {
          reject(err);
        });
      });
    }

    return Promise.reject(err);
  }
);

// cb, errCb : 콜백 함수
const subscribeTokenRefresh = (cb, errCb) => {
  refreshSubcribers.push({ cb, errCb });
};

const processQueue = (err, token=null) => {
  refreshSubcribers.forEach((subscriber) => {
    if (err) {
      subscriber.errCb(err);
    } else {
      subscriber.cb(token);
    }
  });
  refreshSubcribers = [];
}

export default api;