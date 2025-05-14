package ru.netology.cloudservice.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudservice.dto.FileResponse;
import ru.netology.cloudservice.entity.File;
import ru.netology.cloudservice.entity.User;
import ru.netology.cloudservice.exception.StorageException;
import ru.netology.cloudservice.repository.FileRepository;
import ru.netology.cloudservice.repository.UserRepository;
import ru.netology.cloudservice.util.JwtTokenUtil;
import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${file.storage-path}")
    private String storagePath;
    private Path rootLocation;

    @PostConstruct
    public void init() throws IOException {
        this.rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(rootLocation);
    }


    public void upload(MultipartFile file, String authToken) throws IOException {
        String filename = validateFilename(file);
        User user = getUserFromToken(authToken);
        Path destinationPath = prepareDestinationPath(filename);

        Files.copy(file.getInputStream(), destinationPath);
        saveFileMetadata(file, user, destinationPath);
    }

    public void upload(String filename, byte[] fileData, String authToken) throws IOException {
        User user = getUserFromToken(authToken);
        Path destinationPath = prepareDestinationPath(filename);

        try (InputStream inputStream = new ByteArrayInputStream(fileData)) {
            Files.copy(inputStream, destinationPath);
        }

        saveFileMetadata(filename, user, destinationPath);
    }

    public Resource download(String filename, String authToken) throws IOException {
        User user = getUserFromToken(authToken);
        File fileEntity = getFileFromStorage(filename, user);
        Path filePath = getValidatedFilePath(fileEntity);

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new StorageException("Could not read file: " + filename);
        }

        return resource;
    }

    public void delete(String filename, String authToken) throws IOException {
        User user = getUserFromToken(authToken);
        File file = getFileFromStorage(filename, user);
        Path filePath = getValidatedFilePath(file);

        Files.deleteIfExists(filePath);
        fileRepository.delete(file);
    }

    public List<FileResponse> listFiles(String authToken, int limit) {
        User user = getUserFromToken(authToken);
        return fileRepository.findByUser(user, PageRequest.of(0, limit))
                .stream()
                .map(this::convertToFileResponse)
                .collect(Collectors.toList());
    }

    public void rename(String oldFilename, String newFilename, String authToken) throws IOException {
        if (oldFilename == null || oldFilename.isBlank()) {
            throw new StorageException("Invalid old filename");
        }

        if (newFilename == null || newFilename.isBlank()) {
            throw new StorageException("Invalid new filename");
        }

        if (oldFilename.equals(newFilename)) {
            throw new StorageException("Old and new filenames are the same");
        }

        User user = getUserFromToken(authToken);
        File fileEntity = getFileFromStorage(oldFilename, user);
        Path oldPath = getValidatedFilePath(fileEntity);

        Path newPath = rootLocation.resolve(newFilename).normalize();
        if (!newPath.startsWith(rootLocation)) {
            throw new StorageException("Access denied to target path");
        }


        if (fileRepository.existsByUserAndFilename(user, newFilename)) {
            throw new StorageException("File with name " + newFilename + " already exists for this user");
        }


        if (Files.exists(oldPath)) {
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        } else {
            throw new StorageException("Old file does not exist");
        }


        fileEntity.setFilename(newFilename);
        fileEntity.setFilepath(newPath.toString());
        fileRepository.save(fileEntity);
    }


    private String validateFilename(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new StorageException("Invalid filename");
        }
        return filename;
    }


    private Path prepareDestinationPath(String filename) throws IOException {
        Path destinationPath = rootLocation.resolve(filename).normalize();
        if (!destinationPath.startsWith(rootLocation)) {
            throw new StorageException("Access denied to file path");
        }
        return destinationPath;
    }


    private void saveFileMetadata(MultipartFile file, User user, Path destinationPath) {
        File fileEntity = new File();
        fileEntity.setFilename(file.getOriginalFilename());
        fileEntity.setFilepath(destinationPath.toString());
        fileEntity.setSize(file.getSize());
        fileEntity.setUser(user);
        fileRepository.save(fileEntity);
    }


    private void saveFileMetadata(String filename, User user, Path destinationPath) throws IOException {
        File fileEntity = new File();
        fileEntity.setFilename(filename);
        fileEntity.setFilepath(destinationPath.toString());
        fileEntity.setSize(Files.size(destinationPath));
        fileEntity.setUser(user);
        fileRepository.save(fileEntity);
    }


    private User getUserFromToken(String authToken) {
        String email = jwtTokenUtil.getEmailFromToken(extractToken(authToken));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new StorageException("User not found"));
    }


    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new StorageException("Invalid authorization header");
        }
        return authHeader.substring(7);
    }


    private File getFileFromStorage(String filename, User user) {
        return fileRepository.findByUserAndFilename(user, filename)
                .orElseThrow(() -> new StorageException("File not found: " + filename));
    }


    private Path getValidatedFilePath(File file) {
        Path filePath = Paths.get(file.getFilepath()).normalize();
        if (!filePath.startsWith(rootLocation)) {
            throw new StorageException("Access denied to file path");
        }
        return filePath;
    }


    private FileResponse convertToFileResponse(File file) {
        return new FileResponse(file.getFilename(), file.getSize());
    }
}