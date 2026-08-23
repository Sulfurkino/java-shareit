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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestServiceImplTest {
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    @Test
    void createShouldPersistRequest() {
        UserDto author = userService.create(UserDto.builder().name("A").email("a@ex.com").build());

        ItemRequestDto created = itemRequestService.create(author.getId(),
                ItemRequestDto.builder().description("Need a drill").build());

        assertNotNull(created.getId());
        assertEquals("Need a drill", created.getDescription());
        assertNotNull(created.getCreated());
    }

    @Test
    void getAllByUserShouldReturnNewestFirst() throws InterruptedException {
        UserDto author = userService.create(UserDto.builder().name("A").email("a@ex.com").build());
        ItemRequestDto first = itemRequestService.create(author.getId(),
                ItemRequestDto.builder().description("old drill").build());
        Thread.sleep(10);
        ItemRequestDto second = itemRequestService.create(author.getId(),
                ItemRequestDto.builder().description("new hammer").build());

        List<ItemRequestDto> own = itemRequestService.getAllByUser(author.getId());
        assertEquals(2, own.size());
        assertEquals(second.getId(), own.get(0).getId());
        assertEquals(first.getId(), own.get(1).getId());
    }

    @Test
    void getAllShouldReturnOthersWithOffset() throws InterruptedException {
        UserDto author = userService.create(UserDto.builder().name("A").email("a@ex.com").build());
        UserDto other = userService.create(UserDto.builder().name("B").email("b@ex.com").build());
        ItemRequestDto first = itemRequestService.create(author.getId(),
                ItemRequestDto.builder().description("old drill").build());
        Thread.sleep(10);
        itemRequestService.create(author.getId(),
                ItemRequestDto.builder().description("new hammer").build());

        List<ItemRequestDto> page = itemRequestService.getAll(1, 1, other.getId());
        assertEquals(1, page.size());
        assertEquals(first.getId(), page.get(0).getId());
    }

    @Test
    void createAndGetShouldAttachOfferedItemsNewestFirst() {
        UserDto author = userService.create(UserDto.builder().name("A").email("a@ex.com").build());
        UserDto owner = userService.create(UserDto.builder().name("B").email("b@ex.com").build());
        ItemRequestDto created = itemRequestService.create(author.getId(),
                ItemRequestDto.builder().description("Need a drill").build());
        itemService.create(ItemDto.builder().name("Drill").description("x").available(true)
                .requestId(created.getId()).build(), owner.getId());

        ItemRequestDto found = itemRequestService.getById(created.getId(), owner.getId());
        assertEquals("Need a drill", found.getDescription());
        assertEquals(1, found.getItems().size());
        assertEquals(owner.getId(), found.getItems().get(0).getOwnerId());
    }
}
