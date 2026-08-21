package ru.practicum.shareit.repositorytests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.model.BookingStatus.WAITING;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingController bookingController;

    @Autowired
    private UserController userController;

    @Autowired
    private ItemController itemController;

    @Test
    void shouldCreateApproveAndListBooking() {
        UserDto owner = userController.create(user("Owner", "owner@example.com"));
        UserDto booker = userController.create(user("Booker", "booker@example.com"));
        ItemDto item = itemController.create(owner.getId(), item(true));
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        BookingDto booking = bookingController.create(booking(item.getId(), start, start.plusDays(1)),
                booker.getId());

        assertEquals(WAITING, booking.getStatus());
        assertEquals(1, bookingController.getAllByBooker(booker.getId(), "WAITING").size());
        assertEquals(1, bookingController.getAllByOwner(owner.getId(), "ALL").size());

        BookingDto approved = bookingController.approve(booking.getId(), owner.getId(), true);
        assertEquals(APPROVED, approved.getStatus());
        assertEquals(booking.getId(), bookingController.getById(booking.getId(), booker.getId()).getId());
    }

    @Test
    void shouldEnforceBookingRules() {
        UserDto owner = userController.create(user("Owner", "owner@example.com"));
        UserDto anotherUser = userController.create(user("Booker", "booker@example.com"));
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        ItemDto availableItem = itemController.create(owner.getId(), item(true));
        ItemDto unavailableItem = itemController.create(owner.getId(), item(false));

        assertThrows(NotFoundException.class,
                () -> bookingController.create(booking(availableItem.getId(), start, start.plusDays(1)),
                        owner.getId()));
        assertThrows(BadRequestException.class,
                () -> bookingController.create(booking(unavailableItem.getId(), start, start.plusDays(1)),
                        anotherUser.getId()));
        assertThrows(BadRequestException.class,
                () -> bookingController.getAllByBooker(anotherUser.getId(), "unsupported"));
    }

    @Test
    void shouldReturnForbiddenWhenBookingApprovedByWrongUser() throws Exception {
        UserDto owner = userController.create(user("Owner", "owner@example.com"));
        UserDto booker = userController.create(user("Booker", "booker@example.com"));
        UserDto wrongUser = userController.create(user("Wrong", "wrong@example.com"));
        ItemDto item = itemController.create(owner.getId(), item(true));
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingDto booking = bookingController.create(booking(item.getId(), start, start.plusDays(1)),
                booker.getId());

        mockMvc.perform(patch("/bookings/{bookingId}", booking.getId())
                        .header("X-Sharer-User-Id", wrongUser.getId())
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    private UserDto user(String name, String email) {
        return UserDto.builder().name(name).email(email).build();
    }

    private ItemDto item(boolean available) {
        return ItemDto.builder()
                .name("Drill")
                .description("Cordless drill")
                .available(available)
                .build();
    }

    private BookingShortDto booking(Long itemId, LocalDateTime start, LocalDateTime end) {
        return BookingShortDto.builder().itemId(itemId).start(start).end(end).build();
    }
}
