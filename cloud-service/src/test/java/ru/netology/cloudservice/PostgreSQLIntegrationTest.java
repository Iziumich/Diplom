package ru.netology.cloudservice;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.netology.cloudservice.config.TestContainersConfig;
import ru.netology.cloudservice.repository.UserRepository;
import ru.netology.cloudservice.repository.FileRepository;

@Testcontainers
@ContextConfiguration(initializers = TestContainersConfig.Initializer.class)
@DataJpaTest
public abstract class PostgreSQLIntegrationTest {

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected FileRepository fileRepository;
    protected String testEmail = "admin@admin.admin";
    protected String testPassword = "password";
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @BeforeEach
    void setUp() {
        fileRepository.deleteAll();
        userRepository.deleteAll();
    }
}