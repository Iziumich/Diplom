package ru.netology.cloudservice.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "Login is required")
    private String login;
    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest(String testEmail, String testPassword) {

    }
}