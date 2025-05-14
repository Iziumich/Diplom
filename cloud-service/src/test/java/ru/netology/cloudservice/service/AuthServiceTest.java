package ru.netology.cloudservice.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import ru.netology.cloudservice.CloudServiceApplication;
import ru.netology.cloudservice.PostgreSQLIntegrationTest;
import ru.netology.cloudservice.dto.LoginRequest;
import ru.netology.cloudservice.entity.User;
import ru.netology.cloudservice.repository.UserRepository;
import ru.netology.cloudservice.util.JwtTokenUtil;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = CloudServiceApplication.class)
@DataJpaTest
class AuthServiceTest extends PostgreSQLIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    private final String testEmail = "admin@admin.admin";
    private final String testPassword = "password";

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtTokenUtil, new BCryptPasswordEncoder());
    }

    @Test
    void shouldAuthenticateUserSuccessfully() {
        User user = new User();
        user.setEmail(testEmail);
        user.setPassword(passwordEncoder.encode(testPassword));
        user.setLogin("admin");
        userRepository.save(user);

        when(passwordEncoder.matches(eq(testPassword), anyString())).thenReturn(true);
        when(jwtTokenUtil.generateToken(testEmail)).thenReturn("mocked-token");

        LoginRequest request = new LoginRequest(testEmail, testPassword);
        String token = authService.authenticate(request);

        assertThat(token).isEqualTo("mocked-token");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        LoginRequest request = new LoginRequest("notfound@admin.admin", testPassword);

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldThrowExceptionWhenInvalidPassword() {
        User user = new User();
        user.setEmail(testEmail);
        user.setPassword(passwordEncoder.encode("correct"));
        user.setLogin("admin");
        userRepository.save(user);

        LoginRequest request = new LoginRequest(testEmail, "wrong");

        when(passwordEncoder.matches(eq("wrong"), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid password");
    }
}