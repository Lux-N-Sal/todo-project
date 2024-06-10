import axios from "axios"
import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { motion } from "framer-motion"

import styles from "../styles/Login.module.css"

import Input from "../components/Input"
import Button from "../components/Button"
import Spinner from "../components/Spinner"
import api from "../functions/api"

const Login = () => {
  const navigate = useNavigate();
  const [id, setId] = useState("");
  const [pw, setPw] = useState("");
  const [loading, setLoading] = useState(false);

  const handlers = {
    onChangeId:(e) => setId(e.target.value),
    onChangePw:(e) => setPw(e.target.value),

    async onClickLogin(e) {
      e.preventDefault();
      let res;
      setLoading(true);
      res = await axios.post("/api/v1/user/login", {loginId:id, loginPw:pw}, {headers: {
        'Content-Type': 'application/json'
      }});
      if (res.data.resultType === "S") {
        sessionStorage.setItem("accessToken", res.data.body.sessionId);
        api.defaults.headers.common['Authorization'] = 'Bearer ' + res.data.body.sessionId;
        navigate("/");
        setLoading(false);
      } else if (res.data.resultType === "F") {
        switch(res.data.errorCode) {
          case 'LE_001':
            alert("아이디가 존재하지 않습니다.")
            break;
          case 'LE_002':
            alert("비밀번호가 옳바르지 않습니다.");
            break;
          default:
            alert("오류!");
        }
        setLoading(false);
      }
    }
  }

  return (
    <motion.div 
      className={styles.login}
    >
      <div>
      <form action="">
        <Input
          id="id"
          value={id}
          onChange={handlers.onChangeId}
          label={"아이디"}
          style={{marginBottom:"100px"}}
        />
          <br />
          <Input
            id="pw"
            type="password"
            value={pw}
            onChange={handlers.onChangePw}
            label={"비밀번호"}
            style={{marginBottom:"100px"}}
          /><br />
          <span
          className={styles["login-button"]}
          >
            {loading?
              <Spinner />:
              <Button
                text={"로그인"}
                onClick={handlers.onClickLogin}
              />
            }
          </span>
        </form>
      </div>
    </motion.div>
  );
}

export default Login;