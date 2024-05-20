package com.maker.Smart_To_Do_List.exception;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    OK(HttpStatus.OK,""),
    DUPLICATED(HttpStatus.CONFLICT,""),
    NOT_FOUND(HttpStatus.NOT_FOUND,""),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED,""),
    ACCESS_DENIED(HttpStatus.FORBIDDEN,"");


    private HttpStatus httpStatus;
    private String message;
}
