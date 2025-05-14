package ru.netology.cloudservice.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloudservice.dto.LoginRequest;
import ru.netology.cloudservice.entity.User;
import ru.netology.cloudservice.repository.UserRepository;
import ru.netology.cloudservice.util.JwtTokenUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public String authenticate(LoginRequest request) {
        if (request.getLogin() == null || request.getPassword() == null) {
            throw new BadCredentialsException("Login and password must not be null");
        }

        User user = userRepository.findByEmail(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return jwtTokenUtil.generateToken(user.getEmail());
    }

    @Transactional
    public void logout(String token) {
        try {
            String email = jwtTokenUtil.getEmailFromToken(token.replace("Bearer ", ""));
            log.info("User {} logged out", email);
        } catch (Exception e) {
            log.warn("Logout error: {}", e.getMessage());
            throw new BadCredentialsException("Invalid token");
        }
    }
}