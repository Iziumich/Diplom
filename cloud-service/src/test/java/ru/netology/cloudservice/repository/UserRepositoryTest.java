package ru.netology.cloudservice.repository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.netology.cloudservice.entity.User;
import ru.netology.cloudservice.PostgreSQLIntegrationTest;
import javax.validation.constraints.NotBlank;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
class UserRepositoryTest extends PostgreSQLIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    private @NotBlank String testLogin;

    @Test
    void shouldSaveAndFindUserByEmail() {
        User user = new User();
        user.setEmail(testEmail);
        user.setPassword(passwordEncoder.encode(testPassword));
        user.setLogin(testLogin);

        User savedUser = userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail(testEmail);

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(testEmail);
    }

    @Test
    void shouldCheckUserExistsByEmail() {
        User user = new User();
        user.setEmail(testEmail);
        user.setPassword(passwordEncoder.encode(testPassword));
        user.setLogin(testLogin);
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail(testEmail);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldNotFindNonExistentUser() {
        Optional<User> user = userRepository.findByEmail("nonexistent@admin.admin");

        assertThat(user).isEmpty();
    }
}