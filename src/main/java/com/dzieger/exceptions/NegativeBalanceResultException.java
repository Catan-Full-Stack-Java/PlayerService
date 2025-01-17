package com.dzieger.exceptions;

public class NegativeBalanceResultException extends RuntimeException{

    public NegativeBalanceResultException() {
        super("Balance cannot go below zero. Ensure you are not taking out too much.");
    }

    public NegativeBalanceResultException(String message) {
        super(message);
    }

    public NegativeBalanceResultException(String message, Throwable cause) {
        super(message, cause);
    }
}

