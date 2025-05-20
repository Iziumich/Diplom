package ru.netology.cloudservice.exception;

import java.io.IOException;

public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, IOException cause) {
        super(message, cause);
    }
}