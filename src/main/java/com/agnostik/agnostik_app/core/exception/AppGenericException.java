package com.agnostik.agnostik_app.core.exception;

import lombok.Getter;

@Getter
public class AppGenericException extends RuntimeException {

    private final String code;

    public AppGenericException(String code, String message){
        super(message);
        this.code = code;
    }

}
