package com.springcloud.demo.apigateway.exceptions;

import lombok.Getter;

@Getter
public class SimpleException extends  RuntimeException{

    private final int status;

    public SimpleException(int status, String message) {
        super(message);
        this.status = status;
    }

}
