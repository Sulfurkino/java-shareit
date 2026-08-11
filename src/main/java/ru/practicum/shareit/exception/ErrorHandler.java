package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");
        log.error(message, ex);
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exception) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
    }

    @ExceptionHandler({DuplicateEmailException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException exception) {
        String message = exception instanceof DuplicateEmailException
                ? exception.getMessage()
                : "Нарушено ограничение целостности данных";
        return response(HttpStatus.CONFLICT, message, exception);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleThrowable(Throwable exception) {
        String message = exception.getMessage() == null ? "Internal server error" : exception.getMessage();
        return response(HttpStatus.INTERNAL_SERVER_ERROR, message, exception);
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String message, Throwable exception) {
        if (status.is5xxServerError()) {
            log.error(message, exception);
        } else {
            log.warn(message);
        }
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }

}
