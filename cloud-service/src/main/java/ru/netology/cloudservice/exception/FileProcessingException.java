package ru.netology.cloudservice.exception;


public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String message) {
        super(message);
    }
}