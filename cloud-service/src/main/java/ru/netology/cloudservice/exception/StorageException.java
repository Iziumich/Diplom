package ru.netology.cloudservice.exception;

import java.io.IOException;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String fileUploadFailed, IOException e) {
    }
}