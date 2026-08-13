package ru.practicum.shareit.repositorytests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTests {
    @Autowired
    private UserController userController;

    @Test
    void shouldCreateUpdateAndDeleteUser() {
        UserDto created = userController.create(user("George", "george@example.com"));

        assertEquals(created.getId(), userController.getById(created.getId()).getId());

        userController.update(created.getId(), UserDto.builder().email("updated@example.com").build());
        assertEquals("updated@example.com", userController.getById(created.getId()).getEmail());

        userController.delete(created.getId());
        assertEquals(0, userController.getAll().size());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        userController.create(user("George", "same@example.com"));

        assertThrows(DuplicateEmailException.class,
                () -> userController.create(user("Michael", "same@example.com")));
    }

    private UserDto user(String name, String email) {
        return UserDto.builder().name(name).email(email).build();
    }
}
