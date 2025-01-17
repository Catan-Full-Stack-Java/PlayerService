package com.dzieger.exceptions;

public class NegativeBalanceException extends RuntimeException{

    public NegativeBalanceException() {
        super("Balance cannot go below zero. Ensure you are not taking out too much.");
    }

    public NegativeBalanceException(String message) {
        super(message);
    }

    public NegativeBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}

