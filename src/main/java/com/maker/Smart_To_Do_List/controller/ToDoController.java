package com.maker.Smart_To_Do_List.controller;

import com.maker.Smart_To_Do_List.domain.ToDo;
import com.maker.Smart_To_Do_List.domain.ToDoList;
import com.maker.Smart_To_Do_List.dto.*;
import com.maker.Smart_To_Do_List.exception.AppException;
import com.maker.Smart_To_Do_List.exception.ErrorCode;
import com.maker.Smart_To_Do_List.mapper.ToDoMapper;
import com.maker.Smart_To_Do_List.repository.ListRepository;
import com.maker.Smart_To_Do_List.response.TodoResponse;
import com.maker.Smart_To_Do_List.response.TodosResponse;
import com.maker.Smart_To_Do_List.service.JwtService;
import com.maker.Smart_To_Do_List.service.ToDoService;
import com.maker.Smart_To_Do_List.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/list")
public class ToDoController {

    private final ToDoService toDoService;
    private final JwtService jwtService;
    private final UserService userService;
    private final ListRepository listRepository;

    /**
     * POST
     [createToDo]:  todo 생성 API
     **/
    @PostMapping("/{listId}/create")
    public ResponseEntity<TodoResponse> createToDo(HttpServletRequest request,
                                                   @RequestBody CreateToDoRequest createToDoDto,
                                                   @PathVariable("listId") final String listId){

        // 로그인한 jwt 토큰을 통해 userId 추출
        String userId = jwtService.getUserId(request);

        // todo 생성함수 호출
        // 실질적인 생성은 todoService의 creatToDo가 한다.
        TodoResponse todoResponse = toDoService.createToDo(
                userId,
                listId,
                createToDoDto

        );

        // 200과 생성 성공을 반환
        return new ResponseEntity<>(todoResponse, HttpStatus.OK);
    }

    /**
     * GET
     [getToDos]: 특정 list의 todo를 전부 반환
     **/
    @GetMapping(value = "/{listId}/todos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TodosResponse> getToDos(@PathVariable("listId") final String listId,
                                                  HttpServletRequest request){
        
        // 로그인한 jwt 토큰을 통해 userId 추출
        String userId = jwtService.getUserId(request);

        // todo들, list정보를 반환
        TodosResponse result = toDoService.getToDos(
                userId,
                listId
        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * PUT
     [updateToDoValue]: todo updateAPI
     [필요 수정 사항]: 영역별로 API를 나눌 필요성이 있음 ex) status 나누고 .. 등등 1버튼 1API
     ex) 타이틀 수정, 마감시간 수정 등..
     **/
    @PutMapping("/{listId}/{todoId}")
    public ResponseEntity<?> updateToDoValue(HttpServletRequest request,
                                             @RequestBody CreateToDoRequest createToDoRequest,
                                             @PathVariable("listId") final String listId,
                                             @PathVariable("todoId") final String toDoId){

        String userId = jwtService.getUserId(request);

        // updateToDoValue() 안에 리스트-투두 검증 로직있음
        CreateToDoRequest createToDoDto = toDoService.updateToDoValue(
                userId,
                listId,
                toDoId,
                createToDoRequest
        );
        return new ResponseEntity<>(createToDoDto, HttpStatus.OK);
    }

    /**
     * PUT
     [changeStatus]: todo의 상태를 업데이트
     **/
    @PutMapping("/{listId}/{todoId}/status")
    public ResponseEntity<?> changeStatus(HttpServletRequest request,
                                          @RequestBody ChangeStatus changeStatus,
                                          @PathVariable("listId") final String listId,
                                          @PathVariable("todoId") final String toDoId){

        String userId = jwtService.getUserId(request);

        toDoService.changeStatus(
                userId,
                listId,
                toDoId,
                changeStatus
        );

        return new ResponseEntity<>("Change Success", HttpStatus.OK);

    }

    /**
     * Delete
     [deleteToDo]: todo 제거
     **/
    @DeleteMapping("/{listId}/{todoId}")
    public ResponseEntity<Void> deleteToDo(HttpServletRequest request,
                                           @PathVariable("listId") final String listId,
                                           @PathVariable("todoId") final String toDoId) throws IOException {

        String userId = jwtService.getUserId(request);
        toDoService.deleteToDo(
                userId,
                listId,
                toDoId
        );

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
