package ru.netology.cloudservice.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.netology.cloudservice.exception.CorsException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String ALLOWED_ORIGIN = "http://localhost:8081";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/cloud/**")
                .allowedOrigins(ALLOWED_ORIGIN)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "auth-token")
                .exposedHeaders("Authorization", "auth-token")
                .allowCredentials(true)
                .maxAge(3600)
                .allowCredentials(true);
    }


    public void validateOrigin(String origin) throws CorsException {
        if (origin == null || !origin.equals(ALLOWED_ORIGIN)) {
            throw new CorsException("Origin not allowed: " + origin);
        }
    }
}