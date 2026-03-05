package com.andreidombrovschi.tradefinance.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode code;
    private final HttpStatus httpStatus;

    public BusinessException(ErrorCode code, HttpStatus httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
