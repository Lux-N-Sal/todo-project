package com.maker.Smart_To_Do_List.mapper;

import com.maker.Smart_To_Do_List.domain.ToDoList;
import com.maker.Smart_To_Do_List.dto.SortDto;
import com.maker.Smart_To_Do_List.dto.ToDoListDto;
import com.maker.Smart_To_Do_List.dto.ToDoListsDto;

import java.util.List;
import java.util.stream.Collectors;

public class ToDoListMapper {
    public static ToDoListDto convertToDto(ToDoList toDoList){
        ToDoListDto toDoListDto = new ToDoListDto();
        toDoListDto.setListId(toDoList.getListId());
        toDoListDto.setListName(toDoList.getListName());

        return toDoListDto;
    }

    public static ToDoListsDto convertToDtoList(List<ToDoList> toDoLists){
        ToDoListsDto toDoListsDto = new ToDoListsDto();
        toDoListsDto.setToDoListDto(toDoLists.stream().map(ToDoListMapper::convertToDto).collect(Collectors.toList()));
        return toDoListsDto;
    }
}
