package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplLogicTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Owner").email("owner@ex.com").build();
        booker = User.builder().id(2L).name("Booker").email("booker@ex.com").build();
        item = Item.builder()
                .id(10L)
                .name("Drill")
                .description("Cordless")
                .available(true)
                .owner(owner)
                .build();
    }

    @Test
    void getAllAndGetByIdShouldEnrichBookingsOnlyForOwner() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> itemService.getAll(99L));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findAllByOwnerId(1L)).thenReturn(List.of(item));
        when(commentRepository.findAllByItemIdOrderByCreatedAsc(10L)).thenReturn(List.of());
        when(bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                eq(10L), eq(BookingStatus.APPROVED), any())).thenReturn(Optional.empty());
        when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                eq(10L), eq(BookingStatus.APPROVED), any())).thenReturn(Optional.empty());

        assertEquals(1, itemService.getAll(1L).size());

        when(itemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.getById(10L, 1L));

        Booking last = Booking.builder()
                .id(3L).item(item).booker(booker).status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(2)).end(LocalDateTime.now().minusDays(1)).build();
        Booking next = Booking.builder()
                .id(4L).item(item).booker(booker).status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2)).build();
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                eq(10L), eq(BookingStatus.APPROVED), any())).thenReturn(Optional.of(last));
        when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                eq(10L), eq(BookingStatus.APPROVED), any())).thenReturn(Optional.of(next));

        ItemDto ownerView = itemService.getById(10L, 1L);
        assertNotNull(ownerView.getLastBooking());
        assertNotNull(ownerView.getNextBooking());

        ItemDto strangerView = itemService.getById(10L, 2L);
        assertNull(strangerView.getLastBooking());
        assertNull(strangerView.getNextBooking());
    }

    @Test
    void createUpdateDeleteAndSearchShouldCoverBranches() {
        ItemDto dto = ItemDto.builder().name("Drill").description("x").available(true).requestId(7L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.create(dto, 1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.create(dto, 1L));

        ItemRequest request = ItemRequest.builder().id(7L).description("need").requestor(booker).build();
        when(itemRequestRepository.findById(7L)).thenReturn(Optional.of(request));
        doAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        }).when(itemRepository).save(any());
        assertEquals(7L, itemService.create(dto, 1L).getRequestId());

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        assertThrows(NotFoundException.class, () -> itemService.update(
                ItemDto.builder().name("Other").build(), 10L, 2L));

        doAnswer(invocation -> invocation.getArgument(0)).when(itemRepository).save(any());
        ItemDto updated = itemService.update(ItemDto.builder()
                .name("Updated").description("d").available(false).build(), 10L, 1L);
        assertEquals("Updated", updated.getName());
        assertEquals(Boolean.FALSE, updated.getAvailable());

        itemService.delete(10L);
        verify(itemRepository).delete(item);

        assertTrue(itemService.search(null).isEmpty());
        assertTrue(itemService.search("  ").isEmpty());
        when(itemRepository
                .findAllByAvailableTrueAndNameContainingIgnoreCaseOrAvailableTrueAndDescriptionContainingIgnoreCase(
                        "drill", "drill")).thenReturn(List.of(item));
        assertEquals(1, itemService.search("drill").size());
    }

    @Test
    void createCommentShouldRequireCompletedBooking() {
        CommentDto commentDto = CommentDto.builder().text("nice").build();
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.createComment(10L, 2L, commentDto));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(2L), eq(10L), eq(BookingStatus.APPROVED), any())).thenReturn(false);
        assertThrows(BadRequestException.class, () -> itemService.createComment(10L, 2L, commentDto));

        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(2L), eq(10L), eq(BookingStatus.APPROVED), any())).thenReturn(true);
        doAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(8L);
            return comment;
        }).when(commentRepository).save(any());
        assertEquals("nice", itemService.createComment(10L, 2L, commentDto).getText());
        assertEquals("Booker", itemService.createComment(10L, 2L, commentDto).getAuthorName());
    }
}
