package com.andreidombrovschi.tradefinance.common.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {
    public ConflictException(ErrorCode code, String message) {
        super(code, HttpStatus.CONFLICT, message);
    }
}