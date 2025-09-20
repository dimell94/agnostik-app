package com.agnostik.agnostik_app.core.exception;

public class AppObjectAlreadyExistsException extends AppGenericException {

    public AppObjectAlreadyExistsException(String message){
        super("OBJECT_ALREADY_EXISTS", message);
    }

}
