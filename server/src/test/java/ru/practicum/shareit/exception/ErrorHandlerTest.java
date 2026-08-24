package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ErrorHandlerTest {
    private final ErrorHandler handler = new ErrorHandler();

    @Test
    void shouldMapDomainExceptions() {
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handleNotFoundException(new NotFoundException("missing")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleBadRequestException(new BadRequestException("bad")).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                handler.handleAccessDeniedException(new AccessDeniedException("no")).getStatusCode());
        assertEquals("dup",
                handler.handleConflict(new DuplicateEmailException("dup")).getBody().getError());
        assertEquals("Нарушено ограничение целостности данных",
                handler.handleConflict(new DataIntegrityViolationException("x")).getBody().getError());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                handler.handleThrowable(new RuntimeException("boom")).getStatusCode());
        assertEquals("Internal server error",
                handler.handleThrowable(new RuntimeException()).getBody().getError());
    }

    @Test
    void shouldMapValidationErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
                UserDto.builder().build(), "userDto");
        bindingResult.addError(new FieldError("userDto", "email", "must not be blank"));
        MethodParameter parameter = new MethodParameter(
                UserController.class.getDeclaredMethod("create", UserDto.class), 0);
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals("email: must not be blank", body.getError());

        BeanPropertyBindingResult empty = new BeanPropertyBindingResult(
                UserDto.builder().build(), "userDto");
        MethodArgumentNotValidException emptyException =
                new MethodArgumentNotValidException(parameter, empty);
        ErrorResponse fallback = (ErrorResponse) handler.handleMethodArgumentNotValid(
                emptyException, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class)).getBody();
        assertEquals("Validation failed", fallback.getError());
    }

    @Test
    void shouldCoverErrorResponseConstructors() {
        ErrorResponse fromMessage = new ErrorResponse("only-error");
        assertEquals("only-error", fromMessage.getError());
        ErrorResponse empty = new ErrorResponse();
        empty.setStatusCode(400);
        empty.setMessage("m");
        empty.setError("e");
        assertEquals(400, empty.getStatusCode());
        assertEquals("m", empty.getMessage());
        assertEquals("e", empty.getError());
    }
}
