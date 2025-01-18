package com.dzieger.exceptions;

public class ProfileAlreadyExistsException extends RuntimeException {

    public ProfileAlreadyExistsException(String message) {
        super(message);
    }

    public ProfileAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfileAlreadyExistsException() {
        super("Profile already exists");
    }
}
