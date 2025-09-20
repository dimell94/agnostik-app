package com.agnostik.agnostik_app.core.exception;

public class AppInvalidCredentialsException extends AppGenericException {

    public AppInvalidCredentialsException(){
        super("BAD_CREDENTIALS", "Incorrect username or password.");
    }
}
