package ru.netology.cloudservice.exception;

public class CorsException extends RuntimeException {
    public CorsException(String message) {
        super(message);
    }

}