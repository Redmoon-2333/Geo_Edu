package com.geoedu.exception;

public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(401, message);
    }
}
