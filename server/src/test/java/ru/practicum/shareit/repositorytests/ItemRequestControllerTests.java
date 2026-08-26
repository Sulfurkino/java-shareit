package ru.practicum.shareit.repositorytests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestControllerTests {
    @Autowired
    private ItemRequestController itemRequestController;

    @Autowired
    private UserController userController;

    @Autowired
    private ItemController itemController;

    @Test
    void shouldCreateAndGetRequestById() {
        UserDto user = userController.create(user("George", "george@example.com"));
        ItemRequestDto created = itemRequestController.create(user.getId(), request("Need a drill"));

        ItemRequestDto found = itemRequestController.getById(created.getId(), user.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals("Need a drill", found.getDescription());
        assertTrue(found.getItems().isEmpty());
    }

    @Test
    void shouldReturnOwnRequestsAndExcludeThemFromAll() {
        UserDto author = userController.create(user("George", "george@example.com"));
        UserDto other = userController.create(user("Michael", "michael@example.com"));
        ItemRequestDto created = itemRequestController.create(author.getId(), request("Need a drill"));

        assertEquals(1, itemRequestController.getAllByUser(author.getId()).size());
        assertEquals(0, itemRequestController.getAll(0, 10, author.getId()).size());
        assertEquals(1, itemRequestController.getAll(0, 10, other.getId()).size());
        assertEquals(created.getId(), itemRequestController.getAll(0, 10, other.getId()).get(0).getId());
    }

    @Test
    void shouldAttachOfferedItemsToRequest() {
        UserDto author = userController.create(user("George", "george@example.com"));
        UserDto owner = userController.create(user("Michael", "michael@example.com"));
        ItemRequestDto request = itemRequestController.create(author.getId(), request("Need a drill"));

        itemController.create(owner.getId(), ItemDto.builder()
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .requestId(request.getId())
                .build());

        ItemRequestDto found = itemRequestController.getById(request.getId(), author.getId());
        assertEquals(1, found.getItems().size());
        assertEquals("Drill", found.getItems().get(0).getName());
        assertEquals(request.getId(), found.getItems().get(0).getRequestId());
    }

    @Test
    void shouldReturnRequestsNewestFirstAndSkipByOffset() throws InterruptedException {
        UserDto author = userController.create(user("George", "george@example.com"));
        UserDto other = userController.create(user("Michael", "michael@example.com"));
        ItemRequestDto first = itemRequestController.create(author.getId(), request("old drill"));
        Thread.sleep(10);
        ItemRequestDto second = itemRequestController.create(author.getId(), request("new hammer"));

        List<ItemRequestDto> own = itemRequestController.getAllByUser(author.getId());
        assertEquals(2, own.size());
        assertEquals(second.getId(), own.get(0).getId());
        assertEquals(first.getId(), own.get(1).getId());

        List<ItemRequestDto> page = itemRequestController.getAll(1, 1, other.getId());
        assertEquals(1, page.size());
        assertEquals(first.getId(), page.get(0).getId());
    }

    @Test
    void shouldRejectUnknownUserAndInvalidPagination() {
        assertThrows(NotFoundException.class,
                () -> itemRequestController.create(1L, request("Need a drill")));
        assertThrows(NotFoundException.class, () -> itemRequestController.getAllByUser(1L));
        assertThrows(NotFoundException.class, () -> itemRequestController.getAll(0, 10, 1L));
        assertThrows(NotFoundException.class, () -> itemRequestController.getById(1L, 1L));
        assertThrows(BadRequestException.class, () -> itemRequestController.getAll(-1, 10, 1L));
        assertThrows(BadRequestException.class, () -> itemRequestController.getAll(0, 0, 1L));
    }

    private UserDto user(String name, String email) {
        return UserDto.builder().name(name).email(email).build();
    }

    private ItemRequestDto request(String description) {
        return ItemRequestDto.builder().description(description).build();
    }
}
