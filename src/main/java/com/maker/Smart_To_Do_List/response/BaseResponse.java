package com.maker.Smart_To_Do_List.response;

import com.maker.Smart_To_Do_List.enums.ErrCode;
import com.maker.Smart_To_Do_List.enums.ResultType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseResponse<T> {
    private ResultType resultType;
    private ErrCode errorCode;
    private String error;
    private T body;
    private String token;
}
