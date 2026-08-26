package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingServiceImplTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    @Test
    void createApproveAndListByBookerShouldWork() {
        UserDto owner = userService.create(UserDto.builder().name("Owner").email("owner@ex.com").build());
        UserDto booker = userService.create(UserDto.builder().name("Booker").email("booker@ex.com").build());
        ItemDto item = itemService.create(ItemDto.builder()
                .name("Drill")
                .description("Cordless")
                .available(true)
                .build(), owner.getId());

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingShortDto request = BookingShortDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build();

        BookingDto created = bookingService.create(request, booker.getId());
        assertEquals(BookingStatus.WAITING, created.getStatus());

        BookingDto approved = bookingService.approve(created.getId(), owner.getId(), true);
        assertEquals(BookingStatus.APPROVED, approved.getStatus());

        List<BookingDto> bookings = bookingService.getAllByBooker(booker.getId(), State.ALL);
        assertEquals(1, bookings.size());
        assertEquals(created.getId(), bookings.get(0).getId());
        assertEquals(BookingStatus.APPROVED, bookings.get(0).getStatus());
    }

    @Test
    void bookingOwnItemShouldThrowNotFound() {
        UserDto owner = userService.create(UserDto.builder().name("Owner").email("owner@ex.com").build());
        ItemDto item = itemService.create(ItemDto.builder()
                .name("Drill")
                .description("Cordless")
                .available(true)
                .build(), owner.getId());

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingShortDto request = BookingShortDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build();

        assertThrows(NotFoundException.class, () -> bookingService.create(request, owner.getId()));
    }

    @Test
    void bookingUnavailableItemShouldThrowBadRequest() {
        UserDto owner = userService.create(UserDto.builder().name("Owner").email("owner@ex.com").build());
        UserDto booker = userService.create(UserDto.builder().name("Booker").email("booker@ex.com").build());
        ItemDto item = itemService.create(ItemDto.builder()
                .name("Drill")
                .description("Broken")
                .available(false)
                .build(), owner.getId());

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingShortDto request = BookingShortDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build();

        assertThrows(BadRequestException.class, () -> bookingService.create(request, booker.getId()));
    }
}
