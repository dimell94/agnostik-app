package com.agnostik.agnostik_app.core.exception;

public class AppObjectInvalidArgumentException extends AppGenericException{
    public AppObjectInvalidArgumentException(String code, String message){
        super(code, message);
    }
}
