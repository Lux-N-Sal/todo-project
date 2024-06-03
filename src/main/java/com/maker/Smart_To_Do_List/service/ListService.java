package com.maker.Smart_To_Do_List.service;

import com.maker.Smart_To_Do_List.domain.ToDoList;
import com.maker.Smart_To_Do_List.domain.User;
import com.maker.Smart_To_Do_List.dto.ChangeListNameRequest;
import com.maker.Smart_To_Do_List.dto.ToDoListDto;
import com.maker.Smart_To_Do_List.dto.ToDoListsDto;
import com.maker.Smart_To_Do_List.enums.ErrCode;
import com.maker.Smart_To_Do_List.enums.ResultType;
import com.maker.Smart_To_Do_List.mapper.ToDoListMapper;
import com.maker.Smart_To_Do_List.repository.ListRepository;
import com.maker.Smart_To_Do_List.repository.UserRepository;
import com.maker.Smart_To_Do_List.response.ToDoListResponse;
import com.maker.Smart_To_Do_List.response.ToDoListsResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ListService {

    private final ListRepository listRepository;
    private final UserRepository userRepository;
    private final VerificationService verificationService;

    /**
     [createList]: ToDoList를 생성하는 서비스
     listName: 리스트 이름
     userId: 생성한 유저의 아이디
     **/
    public ToDoListResponse createList(String listName, String userId){

        // 빈 ToDoListsResponse 객체 생성
        ToDoListResponse toDoListResponse = new ToDoListResponse();

        // 리스트 이름 검증
        ErrCode checkRes = verificationService.checkListNameOk(userId, listName);

        // 검증 결과에 따른 처리
        switch (checkRes) {
            // 이름 중복
            case LIE_001:
                toDoListResponse.setResultType(ResultType.F);
                toDoListResponse.setErrorCode(ErrCode.LIE_001);
                toDoListResponse.setError(ErrCode.LIE_001.getError());
                toDoListResponse.setBody(new ToDoListDto());
                break;

            // 잘못된 형식의 이름
            case LIE_002:
                toDoListResponse.setResultType(ResultType.F);
                toDoListResponse.setErrorCode(ErrCode.LIE_002);
                toDoListResponse.setError(ErrCode.LIE_002.getError());
                toDoListResponse.setBody(new ToDoListDto());
                break;

            // 정상
            case OK:
                User user = verificationService.findUser(userId);
                ToDoList toDoList = ToDoList.builder()
                        .listId(UUID.randomUUID().toString().replace("-", ""))
                        .listName(listName)
                        .user(user)
                        .build();

                // DB에 저장
                ToDoList savedTodo = listRepository.save(toDoList);
                toDoListResponse.setResultType(ResultType.S);
                toDoListResponse.setErrorCode(ErrCode.OK);
                toDoListResponse.setError(ErrCode.OK.getError());
                toDoListResponse.setBody(ToDoListMapper.convertToDto(savedTodo));
                break;
        }

        return toDoListResponse;
    }

    /**
     [getToDoLists]: 특정 유저가 소유한 ToDoList를 모두 조회하는 서비스
     userId: ToDoList를 조회할 유저의 아이디
     **/
    public ToDoListsResponse getToDoLists(String userId){

        List<ToDoList> toDoLists = listRepository.findByUser_UserIdOrderByCreatedDateAsc(userId);

        ToDoListsResponse toDoListsResponse = new ToDoListsResponse();

        toDoListsResponse.setResultType(ResultType.S);
        toDoListsResponse.setBody(ToDoListMapper.convertToDtoList(toDoLists));
        toDoListsResponse.setErrorCode(ErrCode.OK);
        toDoListsResponse.setError(ErrCode.OK.getError());

        return toDoListsResponse;
    }

    /**
     [getToDoList]: ToDoList를 조회하는 서비스
     userId: ToDoList를 조회할 유저의 아이디
     listId: 조회할 ToDoList의 아이디
     **/
//    public ToDoListDto getToDoList(String userId, String listId){
//
//        // 접근한 ToDoList에 대해 접근자와 소유자가 동일한지 검증
//        verificationService.checkListUser(
//                userId,
//                listId
//        );
//
//        // ToDoList 조회 및 검증
//        ToDoList selectList = verificationService.foundList(listId);
//        return ToDoListMapper.convertToDto(selectList);
//
//    }


    /**
     [changeListName]: ToDoList 이름을 변경하는 서비스
     userId: ToDoList 이름 변경을 요청한 유저의 아이디
     listId: 변경할 ToDoList의 아이디
     changeListNameRequest: 바꿀 이름
     **/
    public ToDoListsResponse changeListName(String userId,String listId,ChangeListNameRequest changeListNameRequest){

        ToDoListsResponse toDoListsResponse = new ToDoListsResponse();

        String listName = changeListNameRequest.getChangeListName();

        // 변경할 ToDoList에 대해 접근자와 소유자가 동일한지 검증
        boolean isOwner = verificationService.checkListUser(
                userId,
                listId
        );

        // 소유자가 아닌 경우
        if (!isOwner) {
            toDoListsResponse.setResultType(ResultType.F);
            toDoListsResponse.setErrorCode(ErrCode.AE_001);
            toDoListsResponse.setError(ErrCode.AE_001.getError());
            toDoListsResponse.setBody(new ToDoListsDto());
        } else {
            // 리스트 이름 검증
            ErrCode checkRes = verificationService.checkListNameOk(userId, listName);

            // 검증 결과에 따른 처리
            switch (checkRes) {
                // 이름 중복
                case LIE_001:
                    toDoListsResponse.setResultType(ResultType.F);
                    toDoListsResponse.setErrorCode(ErrCode.LIE_001);
                    toDoListsResponse.setError(ErrCode.LIE_001.getError());
                    toDoListsResponse.setBody(new ToDoListsDto());
                    break;

                // 잘못된 형식의 이름
                case LIE_002:
                    toDoListsResponse.setResultType(ResultType.F);
                    toDoListsResponse.setErrorCode(ErrCode.LIE_002);
                    toDoListsResponse.setError(ErrCode.LIE_002.getError());
                    toDoListsResponse.setBody(new ToDoListsDto());
                    break;

                // 정상
                case OK:
                    ToDoList updateToDoList = verificationService.foundList(listId);

                    // ToDoList 이름 변경
                    updateToDoList.setListName(changeListNameRequest.getChangeListName());
                    listRepository.save(updateToDoList);

                    return getToDoLists(userId);
            }
        }
        return toDoListsResponse;
    }

    /**
     [deleteToDoList]: ToDoList 삭제 서비스
     userId: ToDoList 삭제 요청한 유저의 아이디
     listId: 삭제할 ToDoList의 아이디
     **/
    public ToDoListsResponse deleteToDoList(String userId, String listId) throws IOException{
        // 삭제할 ToDoList에 대해 접근자와 소유자가 동일한지 검증
        boolean isOwner = verificationService.checkListUser(
                userId,
                listId
        );

        // 검증 완료
        if (isOwner) {
            listRepository.deleteById(listId);

            return getToDoLists(userId);

        // 검증 실패
        } else {
            ToDoListsResponse toDoListsResponse = new ToDoListsResponse();

            toDoListsResponse.setResultType(ResultType.F);
            toDoListsResponse.setErrorCode(ErrCode.AE_001);
            toDoListsResponse.setError(ErrCode.AE_001.getError());
            toDoListsResponse.setBody(new ToDoListsDto());

            return toDoListsResponse;
        }
    }
}
