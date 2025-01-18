package com.dzieger.exceptions;

public class InsufficientFundsException extends RuntimeException{

    public InsufficientFundsException() {
        super("Balance cannot go below zero. Ensure you are not taking out too much.");
    }

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}

