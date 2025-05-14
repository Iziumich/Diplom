package ru.netology.cloudservice.service;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudservice.CloudServiceApplication;
import ru.netology.cloudservice.PostgreSQLIntegrationTest;
import ru.netology.cloudservice.dto.FileResponse;
import ru.netology.cloudservice.entity.File;
import ru.netology.cloudservice.entity.User;
import ru.netology.cloudservice.exception.StorageException;
import ru.netology.cloudservice.repository.FileRepository;
import ru.netology.cloudservice.repository.UserRepository;
import ru.netology.cloudservice.util.JwtTokenUtil;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = CloudServiceApplication.class)
@DataJpaTest
class FileServiceTest extends PostgreSQLIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private FileService fileService;

    private final String testEmail = "admin@admin.admin";
    private final String testAuthToken = "Bearer valid-token";
    private final String storagePath = "build/test-storage";

    @BeforeEach
    void setUp() throws IOException {
        Path rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        if (Files.exists(rootLocation)) {
            Files.walk(rootLocation)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } else {
            Files.createDirectories(rootLocation);
        }

        User user = new User();
        user.setEmail(testEmail);
        user.setPassword("encoded-password");
        user.setLogin("admin");
        userRepository.save(user);

        when(jwtTokenUtil.getEmailFromToken(anyString())).thenReturn(testEmail);

        fileService = new FileService(fileRepository, userRepository, jwtTokenUtil);
    }

    @Test
    void shouldUploadAndListFiles() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        fileService.upload(file, testAuthToken);
        List<FileResponse> files = fileService.listFiles(testAuthToken, 10);

        assertThat(files).hasSize(1);
        assertThat(files.get(0).getFilename()).isEqualTo("test.txt");
        assertThat(Paths.get(storagePath, "test.txt")).exists();
    }

    @Test
    void shouldRenameFile() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file",
                "oldname.txt",
                "text/plain",
                "Hello World".getBytes()
        );
        fileService.upload(file, testAuthToken);

        fileService.rename("oldname.txt", "newname.txt", testAuthToken);

        Path oldPath = Paths.get(storagePath, "oldname.txt");
        Path newPath = Paths.get(storagePath, "newname.txt");

        assertThat(newPath).exists();
        assertThat(oldPath).doesNotExist();

        User user = userRepository.findByEmail(testEmail).orElseThrow();
        assertThat(fileRepository.findByUserAndFilename(user, "newname.txt")).isPresent();
        assertThat(fileRepository.findByUserAndFilename(user, "oldname.txt")).isEmpty();
    }

    @Test
    void shouldDeleteFile() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file",
                "delete.txt",
                "text/plain",
                "ToDelete".getBytes()
        );
        fileService.upload(file, testAuthToken);
        fileService.delete("delete.txt", testAuthToken);
        Path deletedPath = Paths.get(storagePath, "delete.txt");
        User user = userRepository.findByEmail(testEmail).orElseThrow();
        File deletedEntity = fileRepository.findByUserAndFilename(user, "delete.txt").orElse(null);
        assertThat(deletedEntity).isNull();
        assertThat(Files.exists(deletedPath)).isFalse();
    }

    @Test
    void shouldNotAllowDuplicateFilenames() throws IOException {
        MultipartFile file1 = new MockMultipartFile(
                "file",
                "dup.txt",
                "text/plain",
                "Dup1".getBytes()
        );
        fileService.upload(file1, testAuthToken);

        MultipartFile file2 = new MockMultipartFile(
                "file",
                "dup.txt",
                "text/plain",
                "Dup2".getBytes()
        );

        assertThatThrownBy(() -> fileService.upload(file2, testAuthToken))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("already exists");
    }

    @AfterEach
    void tearDown() throws IOException {
        Path rootLocation = Paths.get(storagePath);
        if (Files.exists(rootLocation)) {
            Files.walk(rootLocation)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
}