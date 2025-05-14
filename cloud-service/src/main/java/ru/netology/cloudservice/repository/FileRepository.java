package ru.netology.cloudservice.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.cloudservice.entity.File;
import ru.netology.cloudservice.entity.User;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Page<File> findByUser(User user, Pageable pageable);
    Optional<File> findByUserAndFilename(User user, String filename);
    boolean existsByUserAndFilename(User user, String newFilename);
}