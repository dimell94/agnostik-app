package com.agnostik.agnostik_app.core.exception;

public class AppObjectNotFoundException extends AppGenericException {

    public AppObjectNotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
