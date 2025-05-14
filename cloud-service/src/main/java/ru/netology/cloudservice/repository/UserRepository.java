package ru.netology.cloudservice.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.cloudservice.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}