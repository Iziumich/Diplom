package ru.netology.cloudservice.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudservice.dto.FileResponse;
import ru.netology.cloudservice.dto.RenameFileRequest;
import ru.netology.cloudservice.exception.FileProcessingException;
import ru.netology.cloudservice.exception.StorageException;
import ru.netology.cloudservice.service.FileService;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class FileController {
    private final FileService fileService;

    @GetMapping("/list")
    public ResponseEntity<List<FileResponse>> listFiles(
            @RequestParam(defaultValue = "3") int limit,
            @RequestHeader("auth-token") String token) {
        List<FileResponse> files = fileService.listFiles(token, limit);
        return ResponseEntity.ok(files);
    }

    @PostMapping(value = "/file", consumes = "multipart/form-data")
    public ResponseEntity<Void> uploadFile(
            @RequestParam("filename") String filename,
            @RequestHeader("auth-token") String token,
            @RequestParam("file") MultipartFile file) {
        long maxSizeInBytes = 10 * 1024 * 1024;
        if (file.getSize() > maxSizeInBytes) {
            throw new FileProcessingException("File size exceeds maximum allowed limit of 10MB");
        }
        try {
            fileService.upload(filename, file.getBytes(), token);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new FileProcessingException("File upload failed");
        }
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> download(@RequestParam("filename") String filename,
                                             @RequestHeader("auth-token") String token) throws IOException {
        Resource resource = fileService.download(filename, token);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @DeleteMapping("/file")
    public ResponseEntity<Void> deleteFile(@RequestParam("filename") String filename,
                                           @RequestHeader("auth-token") String token) throws IOException {
        fileService.delete(filename, token);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/file")
    public ResponseEntity<Void> renameFile(
            @RequestParam("filename") String oldFilename,
            @RequestBody RenameFileRequest request,
            @RequestHeader("auth-token") String token) throws IOException {
        String newFilename = request.getFilename();
        if (newFilename == null || newFilename.isBlank()) {
            throw new StorageException("New filename is empty or invalid");
        }
        fileService.rename(oldFilename, newFilename, token);
        return ResponseEntity.ok().build();
    }
}