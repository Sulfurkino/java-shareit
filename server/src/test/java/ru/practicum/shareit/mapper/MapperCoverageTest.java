package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperCoverageTest {
    @Test
    void shouldMapUsersItemsBookingsAndRequests() {
        User user = User.builder().id(1L).name("George").email("g@ex.com").build();
        UserDto userDto = UserMapper.toDTO(user);
        assertEquals("George", userDto.getName());
        assertEquals("g@ex.com", UserMapper.toEntity(userDto).getEmail());

        ItemRequest request = ItemRequest.builder().id(3L).description("need").requestor(user)
                .created(LocalDateTime.now()).build();
        Item item = Item.builder().id(2L).name("Drill").description("x").available(true)
                .owner(user).request(request).build();
        assertEquals(3L, ItemMapper.toItemDto(item).getRequestId());
        assertEquals(3L, ItemMapper.toItemShortDto(item).getRequestId());
        assertEquals(1L, ItemMapper.toItemShortDto(item).getOwnerId());
        assertNull(ItemMapper.toItemDto(Item.builder().id(4L).name("Saw").description("y")
                .available(true).owner(user).build()).getRequestId());
        assertNull(ItemMapper.toItemShortDto(Item.builder().id(4L).name("Saw").description("y")
                .available(true).owner(user).build()).getRequestId());
        ItemDto itemDto = ItemDto.builder().id(2L).name("Drill").description("x").available(true).build();
        assertEquals("Drill", ItemMapper.toItem(itemDto).getName());

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Booking booking = Booking.builder().id(9L).start(start).end(start.plusHours(2))
                .status(BookingStatus.WAITING).item(item).booker(user).build();
        assertEquals(9L, BookingMapper.toBookingDto(booking).getId());
        assertEquals(2L, BookingMapper.toBookingShortDto(booking).getItemId());
        assertEquals(1L, BookingMapper.toBookingShortDto(booking).getBookerId());
        BookingShortDto shortDto = BookingShortDto.builder().start(start).end(start.plusHours(2)).itemId(2L).build();
        assertEquals(start, BookingMapper.toBooking(shortDto).getStart());

        Comment comment = Comment.builder().id(6L).text("ok").author(user).item(item)
                .created(LocalDateTime.now()).build();
        assertEquals("George", CommentMapper.toCommentDto(comment).getAuthorName());
        CommentDto commentDto = CommentDto.builder().id(6L).text("ok").created(LocalDateTime.now()).build();
        assertEquals("ok", CommentMapper.toComment(commentDto).getText());

        ItemRequestDto requestDto = ItemRequestDto.builder().id(3L).description("need").build();
        assertEquals("need", ItemRequestMapper.toItemRequest(requestDto).getDescription());
        assertNotNull(ItemRequestMapper.toItemRequestDto(request).getItems());
    }
}
