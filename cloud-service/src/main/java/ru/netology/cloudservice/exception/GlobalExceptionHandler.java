package ru.netology.cloudservice.exception;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.netology.cloudservice.dto.ErrorResponse;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({
            InvalidTokenException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler({
            UserAlreadyExistsException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler({
            EntityNotFoundException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler({
            CorsException.class
    })
    public ResponseEntity<ErrorResponse> handleCorsException(CorsException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler({
            StorageException.class,
            Exception.class
    })
    public ResponseEntity<ErrorResponse> handleInternalExceptions(RuntimeException ex) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(message, HttpStatus.BAD_REQUEST.value()));
    }
}