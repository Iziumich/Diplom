package ru.netology.cloudservice.exception;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

}