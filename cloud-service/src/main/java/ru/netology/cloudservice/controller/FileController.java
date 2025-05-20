package ru.netology.cloudservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudservice.dto.FileResponse;
import ru.netology.cloudservice.dto.RenameFileRequest;
import ru.netology.cloudservice.exception.StorageException;
import ru.netology.cloudservice.service.FileService;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @GetMapping("/list")
    public ResponseEntity<List<FileResponse>> listFiles(
            @RequestParam(defaultValue = "3") int limit,
            @RequestHeader("auth-token") String token) {
        try {
            List<FileResponse> files = fileService.listFiles(token, limit);
            return ResponseEntity.ok(files);
        } catch (StorageException e) {
            log.error("Error fetching file list", e);
            throw e;
        }
    }

    @PostMapping(value = "/file", consumes = "multipart/form-data")
    public ResponseEntity<Void> uploadFile(
            @RequestParam("filename") String filename,
            @RequestHeader("auth-token") String token,
            @RequestParam("file") MultipartFile file) {
        long maxSizeInBytes = 10 * 1024 * 1024;

        if (file.getSize() > maxSizeInBytes) {
            log.warn("File size exceeds maximum allowed limit: {}", filename);
            throw new StorageException("File size exceeds maximum allowed limit of 10MB");
        }

        try {
            fileService.upload(filename, file.getBytes(), token);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Failed to read file content", e);
            throw new StorageException("File upload failed", e);
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            throw new StorageException("Internal server error", (IOException) e);
        }
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> download(@RequestParam("filename") String filename,
                                             @RequestHeader("auth-token") String token) {
        try {
            Resource resource = fileService.download(filename, token);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading file: {}", filename, e);
            throw new StorageException("File not found or unreadable: " + filename, (IOException) e);
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<Void> deleteFile(@RequestParam("filename") String filename,
                                           @RequestHeader("auth-token") String token) {
        try {
            fileService.delete(filename, token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting file: {}", filename, e);
            throw new StorageException("File deletion failed", (IOException) e);
        }
    }

    @PutMapping("/file")
    public ResponseEntity<Void> renameFile(
            @RequestParam("filename") String oldFilename,
            @RequestBody RenameFileRequest request,
            @RequestHeader("auth-token") String token) {
        String newFilename = request.getFilename();

        if (newFilename == null || newFilename.isBlank()) {
            log.warn("New filename is empty or blank");
            throw new StorageException("New filename is empty or invalid");
        }

        try {
            fileService.rename(oldFilename, newFilename, token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error renaming file from {} to {}", oldFilename, newFilename, e);
            throw new StorageException("File rename failed", (IOException) e);
        }
    }
}