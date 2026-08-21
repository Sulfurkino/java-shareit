package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceImplTest {
    @Autowired
    private UserService userService;

    @Test
    void createAndUpdateEmailShouldSucceed() {
        UserDto created = userService.create(UserDto.builder()
                .name("George")
                .email("george@ex.com")
                .build());

        assertNotNull(created.getId());
        assertEquals("George", created.getName());
        assertEquals("george@ex.com", created.getEmail());

        UserDto updated = userService.update(created.getId(), UserDto.builder()
                .email("george.new@ex.com")
                .build());

        assertEquals(created.getId(), updated.getId());
        assertEquals("George", updated.getName());
        assertEquals("george.new@ex.com", updated.getEmail());
    }

    @Test
    void createWithDuplicateEmailShouldThrow() {
        userService.create(UserDto.builder().name("George").email("dup@ex.com").build());

        assertThrows(DuplicateEmailException.class, () ->
                userService.create(UserDto.builder().name("Michael").email("dup@ex.com").build()));
    }

    @Test
    void updateWithDuplicateEmailShouldThrow() {
        userService.create(UserDto.builder().name("George").email("first@ex.com").build());
        UserDto second = userService.create(UserDto.builder().name("Michael").email("second@ex.com").build());

        assertThrows(DuplicateEmailException.class, () ->
                userService.update(second.getId(), UserDto.builder().email("first@ex.com").build()));
    }
}
