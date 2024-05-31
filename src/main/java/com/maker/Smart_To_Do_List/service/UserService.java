package com.maker.Smart_To_Do_List.service;

import com.maker.Smart_To_Do_List.domain.User;
import com.maker.Smart_To_Do_List.dto.*;
import com.maker.Smart_To_Do_List.enums.ErrCode;
import com.maker.Smart_To_Do_List.enums.ResultType;
import com.maker.Smart_To_Do_List.exception.AppException;
import com.maker.Smart_To_Do_List.exception.ErrorCode;
import com.maker.Smart_To_Do_List.mapper.UserMapper;
import com.maker.Smart_To_Do_List.repository.UserRepository;
import com.maker.Smart_To_Do_List.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.maker.Smart_To_Do_List.auth.JwtUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final VerificationService verificationService;

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     [join]:        신규 유저의 회원가입 서비스
     loginId:       회원가입 유저 아이디
     loginPw:       회원가입 유저 패스워드
     loginPwCheck:  회원가입 패스워드 확인 (사용자가 패스워드를 실수없이 작성했는지 검증)[삭제예정]
     userName:      회원가입 유저 이름
     userEmail:     회원가입 유저 이메일

     기능
     - 패스워드 확인
     - 유저 이름 중복 검증

     return:        {"resultType":"T","error":"","body":null,"errorId":"OK"} (JoinResponse)

     AppException:  유저 이름이 이미 존재하는 경우
                    "패스워드"와 "패스워드 확인"이 다른 경우
     **/
    public JoinResponse join(String loginId, String loginPw, String loginPwCheck, String userName, String userEmail){

        // 수정된 코드: 반환할 joinResponse를 선언
        JoinResponse joinResponse = new JoinResponse();
        joinResponse.setBody("");
        // 유저 아이디(loginId) 중복 검증
//        userRepository.findByLoginId(loginId)
//                .ifPresent(user -> {
//                    // 중복이면 RuntimeException throw하고 ExceptionManger로 이동
//                    throw new AppException(ErrorCode.DUPLICATED, loginId + " is already exits");
//                });

        // 수정한 코드: login id 중복 시 에러 반환
        Optional<User> existUser = userRepository.findByUserId(loginId);

        if(existUser.isPresent()){

            joinResponse.setResultType(ResultType.F);
            joinResponse.setErrorCode(ErrCode.JE_001);
            joinResponse.setError(ErrCode.JE_002.getError()+" : "+loginId);

            return joinResponse;

        }

        // "패스워드"(loginPw)와 "패스워드 확인"(loginPwCheck)이 다르면 예외처리
        if(!loginPw.equals(loginPwCheck)){
//            throw new AppException(ErrorCode.INVALID_PASSWORD, "Password is not match !");
            // 수정된 코드: 비밀번호 다르면 반환
            // 그러나  PasswordCheck 삭제해야한다. > front에서 처리하는게 깔끔함.
            joinResponse.setResultType(ResultType.F);
            joinResponse.setErrorCode(ErrCode.JE_002);
            joinResponse.setError(ErrCode.JE_002.getError());

            return joinResponse;
        }

        // Builder를 통해 User 도메인 생성
        // 패스워드(loginPw)는 암호화되어 저장
        // 정렬기준(sortBy)은 날짜(Date), 정렬방식(OrderBy)은 오름차순(ASC)이 기본 값
        User user = User.builder()
                .userId(UUID.randomUUID().toString().replace("-", ""))
                .loginId(loginId)
                .loginPw(encoder.encode(loginPw))
                .userName(userName)
                .userEmail(userEmail)
                .build();

        // DB에 저장
        userRepository.save(user);

        joinResponse.setResultType(ResultType.S);
        joinResponse.setErrorCode(ErrCode.OK);
        joinResponse.setError(ErrCode.OK.getError());


        return joinResponse;
    }

    /**
     [login]:   유저 로그인 서비스
     loginId:   로그인 아이디
     loginPw:   로그인 패스워드

     return:    LoginResponse
     **/
    
    // 코드 더러움. 매핑을 사용하자
    public LoginResponse login(String loginId, String loginPw, HttpServletResponse response){

        
        final LoginResponse loginResponse = new LoginResponse();
        final LoginDto loginDto = new LoginDto();

        // 로그인 아이디(loginId)를 통해 유저 조회
        Optional<User> user = userRepository.findByLoginId(loginId);
        if(user.isEmpty()){
            loginDto.setSessionId("");
            loginResponse.setResultType(ResultType.F);
            loginResponse.setErrorCode(ErrCode.LE_001);
            loginResponse.setError(ErrCode.LE_001.getError());
            loginResponse.setBody(loginDto);
            return loginResponse;
        }else{
            User selectedUser = user.get();

            // 패스워드가 옳바른지 검증
            final Boolean isValid = verificationService.checkPassword(loginPw,selectedUser);

            if(!isValid){
                loginDto.setSessionId("");
                loginResponse.setResultType(ResultType.F);
                loginResponse.setErrorCode(ErrCode.LE_002);
                loginResponse.setError(ErrCode.LE_002.getError());
                loginResponse.setBody(loginDto);
                return loginResponse;
            }

            final TokenDto tokenDto = JwtUtil.createTokenDto(selectedUser.getLoginId(), secretKey);

            Cookie cookie = new Cookie("refreshToken", tokenDto.getRefreshToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");

            response.addCookie(cookie);


            loginDto.setSessionId(tokenDto.getAccessToken());

            loginResponse.setResultType(ResultType.S);
            loginResponse.setErrorCode(ErrCode.OK);
            loginResponse.setError(ErrCode.OK.getError());
            loginResponse.setBody(loginDto);
            return loginResponse;
        }
    }

    /**
     [getInfo]: 유저 정보 조회 서비스
     user:      조회할 유저

     return:    유저 정보 (UserInfoDto)
     **/
    public UserInfoResponse getInfo(User user){
        // 객체 생성
        UserInfoResponse userInfoResponse = new UserInfoResponse();

        userInfoResponse.setResultType(ResultType.S);
        userInfoResponse.setErrorCode(ErrCode.OK);
        userInfoResponse.setError(ErrCode.OK.getError());
        userInfoResponse.setBody(UserMapper.convertToDto(user));

        return userInfoResponse;
    }

    /**
     [changePassword]:      유저 패스워드 변경 서비스
     userId:                서비스를 이용할 유저의 아이디
     changePasswordRequest: 현재 패스워드와 변경할 패스워드가 담긴 Dto

     return:                EmptyResponse (성공 or 실패)
     **/
    public EmptyResponse changePassword(String userId, ChangePasswordRequest changePasswordRequest){

        EmptyResponse emptyResponse  = new EmptyResponse();
        // 유저 아이디 검증 및 유저 조회
        User user = verificationService.findUser(userId);

        // 유저 패스워드가 옳바른지 검증 (verificationService.checkPassword()랑 기능이 같나? ㅇㅇ)
        if(verificationService.checkListNameDuplicate(changePasswordRequest.getPassword(), user.getLoginPw())){
            // 신규 패스워드로 업데이트
            user.setLoginPw(encoder.encode(changePasswordRequest.getChangePassword()));

            emptyResponse.setResultType(ResultType.S);
            emptyResponse.setError(ErrCode.OK.getError());
            emptyResponse.setErrorCode(ErrCode.OK);

            // DB에 저장
            userRepository.save(user);
        } else {
            emptyResponse.setResultType(ResultType.F);
            emptyResponse.setError(ErrCode.AE_002.getError());
            emptyResponse.setErrorCode(ErrCode.AE_002);
        }

        return emptyResponse;
    }

    /**
     [deleteUser]:      유저 탈퇴 서비스
     userId:            서비스를 이용할 유저의 아이디
     deleteUserRequest: 유저 패스워드가 담긴 Dto
     **/
    public void deleteUser(String userId, DeleteUserRequest deleteUserRequest){
        // 유저 아이디 검증 및 유저 조회
        User selectedUser = verificationService.foundUser(userId);

        // 패스워드가 옳바른지 검증
        verificationService.checkPassword(deleteUserRequest.getLoginPw(),selectedUser);

        // 유저 삭제
        userRepository.deleteById(userId);
    }

    /**
     [getMainToDoListId]:   메인화면에 표시될 ToDoList 조회 서비스
     userId:                서비스를 이용할 유저의 아이디

     return:                메인화면에 표시될 ToDoList의 아이디, 유저 이름이 담긴 Response (MainTodoResponse)
     **/
    public MainTodoResponse getMainToDoListId(String userId){
        // 유저 아이디 검증 및 유저 조회
        User selectedUser = verificationService.findUser(userId);

        MainTodoResponse mainTodoResponse = new MainTodoResponse();

        // 유저정보에서 유저 이름, 메인화면에 표시될 ToDoList의 아이디를 추출하여 ShowMainDto형태로 변환
        mainTodoResponse.setBody(UserMapper.convertToMain(selectedUser));
        mainTodoResponse.setResultType(ResultType.S);
        mainTodoResponse.setError(ErrCode.OK.getError());
        mainTodoResponse.setErrorCode(ErrCode.OK);

        return mainTodoResponse;
    }

    /**
     [updateMainToDoListId]:    메인화면에 표시될 ToDoList 변경 서비스
     userId:                    서비스를 이용할 유저의 아이디
     changeMainListId:          변경할 ToDoList의 아이디

     return:                    메인화면에 표시될 ToDoList의 아이디, 유저 이름이 담긴 Response (MainTodoResponse)
     **/
    public MainTodoResponse updateMainToDoListId(String userId, ChangeMainListId changeMainListId){
        // 유저 아이디 검증 및 유저 조회
        User user = verificationService.findUser(userId);

        MainTodoResponse mainTodoResponse = new MainTodoResponse();

        mainTodoResponse.setBody(UserMapper.convertToMain(user));
        mainTodoResponse.setResultType(ResultType.S);
        mainTodoResponse.setError(ErrCode.OK.getError());
        mainTodoResponse.setErrorCode(ErrCode.OK);

        // 메인화면에 표시될 ToDoList 업데이트
        user.setMainToDoListId(changeMainListId.getMainToDoListId());

        // DB에 저장
        userRepository.save(user);

        return mainTodoResponse;
    }

    /**
     [AuthenticationException]:  AccessToken이 만료됐을 때 RefreshToken으로 AccessToken을 재발급하는 서비스
     refreshToken:               RefreshToken

     return:                    AccessToken
     **/
    public String refresh(String refreshToken) {
        JwtUtil.isExpired(refreshToken, secretKey);
        String accessToken = JwtUtil.createAccessToken(JwtUtil.getLoginId(refreshToken, secretKey), secretKey);

        return accessToken;
    }
}
