package com.dzieger.exceptions;

public class InvalidPreferenceException extends RuntimeException{

    public InvalidPreferenceException() {
        super("Invalid preference");
    }

    public InvalidPreferenceException(String message) {
        super(message);
    }

    public InvalidPreferenceException(String message, Throwable cause) {
        super(message, cause);
    }

}
