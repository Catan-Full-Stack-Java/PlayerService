package com.dzieger.exceptions;

public class FailedConversionToJsonException extends RuntimeException {

    public FailedConversionToJsonException(String message) {
        super(message);
    }

    public FailedConversionToJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedConversionToJsonException() {
        super("Failed to convert object to JSON");
    }
}
