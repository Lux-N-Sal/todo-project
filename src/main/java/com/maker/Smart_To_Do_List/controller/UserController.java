package com.maker.Smart_To_Do_List.controller;


import com.maker.Smart_To_Do_List.domain.User;
import com.maker.Smart_To_Do_List.dto.*;
import com.maker.Smart_To_Do_List.exception.AppException;
import com.maker.Smart_To_Do_List.response.*;
import com.maker.Smart_To_Do_List.service.JwtService;
import com.maker.Smart_To_Do_List.service.ListService;
import com.maker.Smart_To_Do_List.service.UserService;
import com.maker.Smart_To_Do_List.service.VerificationService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final VerificationService verificationService;

    /**
     * POST
     [join]:        회원가입 API

     * RequestBody
     joinDto:       회원가입 시 필요한 정보가 담긴 Dto

     * Response
     HTTP Status:   200 (OK)
     String:        회원가입 성공 Text
     **/
    @PostMapping("/join")
    public ResponseEntity<JoinResponse> join(@RequestBody JoinRequest joinDto){

        // 회원가입 서비스
        final JoinResponse joinResponse = userService.join(
                joinDto.getLoginId(),
                joinDto.getLoginPw(),
                joinDto.getLoginPwCheck(),
                joinDto.getUserName(),
                joinDto.getUserEmail()
        );

        return ResponseEntity.ok().body(joinResponse);
    }

    /**
     * POST
     [checkLoginId]:    회원가입 절차 중, 유저 아이디 중복 검사 API

     * RequestBody
     checkLoginIdDto:   회원가입 유저 아이디가 담긴 Dto

     * Response
     HTTP Status:       200 (OK)
     ?:                 유저 아이디 중복 검사 결과 ([수정할 사항]  <?>로 돼있지만, <Boolean>으로 바꿔도 될 듯?)
     **/
    @PostMapping("/join/id")
    public ResponseEntity<EmptyResponse> checkLoginId(@RequestBody CheckLoginIdDto checkLoginIdDto){
        // 유저 아이디 중복 검사
        final EmptyResponse emptyResponse = verificationService.checkLoginIdDup(checkLoginIdDto.getLoginId());

        return new ResponseEntity<>(emptyResponse, HttpStatus.OK);
    }

    /**
     * POST
     [checkUserName]:   회원가입 절차 중, 유저 이름 중복 검사 API

     * RequestBody
     checkUserNameDto:  회원가입 유저 이름이 담긴 Dto

     * Response
     HTTP Status:       200 (OK)
     ?:                 유저 이름 중복 검사 결과 ([수정할 사항]  <?>로 돼있지만, <Boolean>으로 바꿔도 될 듯?)
     **/
    @PostMapping("/join/username")
    public ResponseEntity<EmptyResponse> checkUserName(@RequestBody CheckUserNameDto checkUserNameDto){
        // 유저 이름 중복 검사
        final EmptyResponse emptyResponse = verificationService.checkUserNameDup(checkUserNameDto.getUserName());

        return new ResponseEntity<>(emptyResponse, HttpStatus.OK);
    }

    /**
     * POST
     [login]:       로그인 API

     * RequestBody
     loginDto:      로그인에 필요한 정보가 담긴 Dto

     * Response
     HTTP Status:   200 (OK)
     TokenDto:      AccessToken, RefreshToken
     **/
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginDto, HttpServletResponse response){

        // 로그인 서비스
//        TokenDto tokenDto = userService.login(
//                loginDto.getLoginId(),
//                loginDto.getLoginPw()
//        );

        final LoginResponse loginResponse = userService.login(
                loginDto.getLoginId(),
                loginDto.getLoginPw(),
                response
        );
// service로 옮김
//        Cookie cookie = new Cookie("refreshToken", tokenDto.getRefreshToken());
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//
//        response.addCookie(cookie);



        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    /**
     * GET
     [getInfo]:     유저 정보 조회 API

     request:       요청

     * Response
     HTTP Status:   200 (OK)
     ?:             유저 정보 ([수정할 사항]  <?>로 돼있지만, <UserDto>로 바꿔도 될 듯?)
     **/
    @GetMapping("/info")
    public ResponseEntity<UserInfoResponse> getInfo(HttpServletRequest request){
        // 요청 헤더에 담긴 Access Token을 통해 유저 조회
        User user = jwtService.getUser(request);

        // 조회된 유저에서 유저 정보를 추출해 Response로 생성
        UserInfoResponse userInfoResponse = userService.getInfo(user);


        return new ResponseEntity<>(userInfoResponse, HttpStatus.OK);
    }

    /**
     * PUT
     [changePassword]:      패스워드 변경 API

     request:               요청

     * RequestBody
     changePasswordRequest: 현재 패스워드와 변경할 패스워드가 담긴 Dto

     * Response
     HTTP Status:           200 (OK)
     ?:                     패스워드 변경 성공 Text ([수정할 사항]  <?>로 돼있지만, <String>으로 바꿔도 될 듯?)
     **/
    @PutMapping("/info")
    public ResponseEntity<EmptyResponse> changePassword(HttpServletRequest request,
                                            @RequestBody ChangePasswordRequest changePasswordRequest){
        // 요청 헤더에 담긴 Access Token을 통해 유저 조회
        String userId = jwtService.getUserId(request);

        // 패스워드 변경 서비스
        EmptyResponse emptyResponse = userService.changePassword(
                userId,
                changePasswordRequest
        );

        return new ResponseEntity<>(emptyResponse, HttpStatus.OK);
    }

    /**
     * DELETE
     [deleteUser]:      유저 탈퇴 API

     request:           요청

     * RequestBody
     deleteUserRequest: 탈퇴할 유저의 패스워드가 담긴 Dto

     * Response
     HTTP Status:       404 (NOT_FOUND)
     **/
    @DeleteMapping("/info")
    public ResponseEntity<Void> deleteUser(HttpServletRequest request,
                                           @RequestBody DeleteUserRequest deleteUserRequest){
        // 요청 헤더에 담긴 Access Token을 통해 유저 조회
        String userId = jwtService.getUserId(request);

        // 유저 탈퇴 서비스
        userService.deleteUser(
                userId,
                deleteUserRequest
        );

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * GET
     [getMainToDoList]: 메인 ToDoList 조회 API

     request:           요청

     * Response
     HTTP Status:       200 (OK)
     ?:                 메인 ToDoList와 유저이름이 담긴 Dto ([수정할 사항]  <?>로 돼있지만, <ShowMainDto>로 바꿔도 될 듯?)
     **/
    @GetMapping("/main")
    public ResponseEntity<MainTodoResponse> getMainToDoList(HttpServletRequest request){
        // 요청 헤더에 담긴 Access Token을 통해 유저 조회
        String userId = jwtService.getUserId(request);

        // 메인 ToDoList 조회 서비스
        MainTodoResponse mainTodoResponse = userService.getMainToDoListId(userId);

        return new ResponseEntity<>(mainTodoResponse, HttpStatus.OK);
    }


    /**
     * PUT
     [updateMainToDoList]:  메인 ToDoList 변경 API

     request:               요청

     * RequestBody
     changeMainListId:      변경할 메인 ToDoList가 담긴 API

     * Response
     HTTP Status:           200 (OK)
     ?:                     메인 TodoList 아이디 ([수정할 사항]  <?>로 돼있지만, <Long>으로 바꿔도 될 듯?)
     **/
    @PutMapping("/main")
    public ResponseEntity<MainTodoResponse> updateMainToDoList (HttpServletRequest request,
                                                 @RequestBody ChangeMainListId changeMainListId){
        // 요청 헤더에 담긴 Access Token을 통해 유저 조회
        String userId = jwtService.getUserId(request);

        // 메인 ToDoList 변경 서비스
        MainTodoResponse mainTodoResponse = userService.updateMainToDoListId(
                userId,
                changeMainListId);

        return new ResponseEntity<>(mainTodoResponse, HttpStatus.OK);
    }

    /**
     * GET
     [refresh]:             AccessToken 재발급 API

     request:               요청

     * Response
     HTTP Status:           200 (OK)
     String:                AccessToken
     **/
    @GetMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletRequest request) throws AuthenticationException {
        Cookie[] cookies = request.getCookies();

        String cookieValue = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    cookieValue = cookie.getValue();
                    break;
                }
            }
        }

        String accessToken = userService.refresh(cookieValue);

        return new ResponseEntity<>(accessToken, HttpStatus.OK);
    }
}
