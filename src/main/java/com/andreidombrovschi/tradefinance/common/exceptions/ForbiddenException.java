package com.andreidombrovschi.tradefinance.common.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(ErrorCode code, String message) {
        super(code, HttpStatus.FORBIDDEN, message);
    }
}
