package ru.practicum.shareit.repositorytests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemControllerTests {
    @Autowired
    private ItemController itemController;

    @Autowired
    private UserController userController;

    @Autowired
    private BookingController bookingController;

    @Test
    void shouldCreateUpdateAndSearchOnlyAvailableItems() {
        UserDto owner = userController.create(user("Owner", "owner@example.com"));
        ItemDto available = itemController.create(owner.getId(), item("Saw", "Electric tool", true));
        itemController.create(owner.getId(), item("Hidden drill", "Unavailable", false));

        itemController.update(ItemDto.builder().description("Updated electric tool").build(),
                available.getId(), owner.getId());

        assertEquals("Updated electric tool",
                itemController.getById(available.getId(), owner.getId()).getDescription());
        assertEquals(1, itemController.search("tool").size());
        assertEquals(0, itemController.search("Hidden").size());
        assertEquals(0, itemController.search(" ").size());
    }

    @Test
    void shouldExposeBookingsOnlyToOwnerAndAllowCommentAfterCompletedBooking() {
        UserDto owner = userController.create(user("Owner", "owner@example.com"));
        UserDto booker = userController.create(user("Booker", "booker@example.com"));
        ItemDto item = itemController.create(owner.getId(), item("Saw", "Electric tool", true));
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        BookingDto booking = bookingController.create(BookingShortDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build(), booker.getId());
        bookingController.approve(booking.getId(), owner.getId(), true);

        itemController.createComment(item.getId(), booker.getId(),
                CommentDto.builder().text("Worked perfectly").build());

        ItemDto ownerView = itemController.getById(item.getId(), owner.getId());
        ItemDto bookerView = itemController.getById(item.getId(), booker.getId());
        assertNotNull(ownerView.getLastBooking());
        assertNull(bookerView.getLastBooking());
        assertEquals(1, ownerView.getComments().size());
        assertEquals("Booker", ownerView.getComments().get(0).getAuthorName());
    }

    @Test
    void shouldRejectCommentWithoutCompletedBooking() {
        UserDto owner = userController.create(user("Owner", "owner@example.com"));
        UserDto user = userController.create(user("User", "user@example.com"));
        ItemDto item = itemController.create(owner.getId(), item("Saw", "Electric tool", true));

        assertThrows(BadRequestException.class, () -> itemController.createComment(
                item.getId(), user.getId(), CommentDto.builder().text("Too early").build()));
    }

    private UserDto user(String name, String email) {
        return UserDto.builder().name(name).email(email).build();
    }

    private ItemDto item(String name, String description, boolean available) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(available)
                .build();
    }
}
