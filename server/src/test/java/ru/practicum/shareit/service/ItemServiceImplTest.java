package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemServiceImplTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemRequestService itemRequestService;

    @Test
    void getAllShouldReturnOnlyOwnerItemsWithOptionalRequestId() {
        UserDto owner = userService.create(UserDto.builder().name("George").email("g@ex.com").build());
        UserDto author = userService.create(UserDto.builder().name("Michael").email("m@ex.com").build());
        ItemRequestDto request = itemRequestService.create(author.getId(),
                ItemRequestDto.builder().description("Need a drill").build());

        itemService.create(ItemDto.builder().name("Saw").description("Hand saw").available(true).build(),
                owner.getId());
        itemService.create(ItemDto.builder().name("Drill").description("Cordless")
                .available(true).requestId(request.getId()).build(), owner.getId());

        var items = itemService.getAll(owner.getId());
        assertEquals(2, items.size());
        assertEquals(request.getId(), items.stream()
                .filter(item -> "Drill".equals(item.getName()))
                .findFirst().orElseThrow()
                .getRequestId());
        assertNull(items.stream()
                .filter(item -> "Saw".equals(item.getName()))
                .findFirst().orElseThrow()
                .getRequestId());
    }
}
