package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplLogicTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private LocalDateTime start;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Owner").email("owner@ex.com").build();
        booker = User.builder().id(2L).name("Booker").email("booker@ex.com").build();
        item = Item.builder().id(10L).name("Drill").description("x").available(true).owner(owner).build();
        start = LocalDateTime.now().plusDays(1);
        booking = Booking.builder()
                .id(5L)
                .start(start)
                .end(start.plusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void createShouldFailWhenUserItemDatesOrAvailabilityInvalid() {
        BookingShortDto dto = BookingShortDto.builder()
                .itemId(10L).start(start).end(start.plusDays(1)).build();

        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.create(dto, 2L));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.create(dto, 2L));

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        assertThrows(NotFoundException.class, () -> bookingService.create(dto, 1L));

        item.setAvailable(false);
        assertThrows(BadRequestException.class, () -> bookingService.create(dto, 2L));

        item.setAvailable(true);
        BookingShortDto invalidDates = BookingShortDto.builder()
                .itemId(10L).start(start).end(start).build();
        assertThrows(BadRequestException.class, () -> bookingService.create(invalidDates, 2L));
    }

    @Test
    void approveShouldValidateOwnerAndStatus() {
        when(bookingRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.approve(5L, 1L, true));

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        assertThrows(AccessDeniedException.class, () -> bookingService.approve(5L, 2L, true));

        booking.setStatus(BookingStatus.APPROVED);
        assertThrows(BadRequestException.class, () -> bookingService.approve(5L, 1L, true));

        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(BookingStatus.REJECTED, bookingService.approve(5L, 1L, false).getStatus());
        booking.setStatus(BookingStatus.WAITING);
        assertEquals(BookingStatus.APPROVED, bookingService.approve(5L, 1L, true).getStatus());
    }

    @Test
    void getByIdShouldAllowOnlyBookerOrOwner() {
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        assertEquals(5L, bookingService.getById(5L, 2L).getId());
        assertEquals(5L, bookingService.getById(5L, 1L).getId());
        assertThrows(NotFoundException.class, () -> bookingService.getById(5L, 99L));
    }

    @Test
    void getAllByBookerShouldCoverEveryState() {
        when(userRepository.existsById(2L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> bookingService.getAllByBooker(2L, State.ALL));

        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findAllByBookerId(eq(2L), any(Sort.class))).thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(
                eq(2L), any(), any(), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findAllByBookerIdAndEndBefore(eq(2L), any(), any(Sort.class)))
                .thenReturn(List.of());
        when(bookingRepository.findAllByBookerIdAndStartAfter(eq(2L), any(), any(Sort.class)))
                .thenReturn(List.of());
        when(bookingRepository.findAllByBookerIdAndStatus(eq(2L), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdAndStatus(eq(2L), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of());

        assertEquals(1, bookingService.getAllByBooker(2L, State.ALL).size());
        assertEquals(0, bookingService.getAllByBooker(2L, State.CURRENT).size());
        assertEquals(0, bookingService.getAllByBooker(2L, State.PAST).size());
        assertEquals(0, bookingService.getAllByBooker(2L, State.FUTURE).size());
        assertEquals(1, bookingService.getAllByBooker(2L, State.WAITING).size());
        assertEquals(0, bookingService.getAllByBooker(2L, State.REJECTED).size());
    }

    @Test
    void getAllByOwnerShouldCoverEveryState() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findAllByItemOwnerId(eq(1L), any(Sort.class))).thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(
                eq(1L), any(), any(), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findAllByItemOwnerIdAndEndBefore(eq(1L), any(), any(Sort.class)))
                .thenReturn(List.of());
        when(bookingRepository.findAllByItemOwnerIdAndStartAfter(eq(1L), any(), any(Sort.class)))
                .thenReturn(List.of());
        when(bookingRepository.findAllByItemOwnerIdAndStatus(eq(1L), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of());
        when(bookingRepository.findAllByItemOwnerIdAndStatus(eq(1L), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of());

        assertEquals(1, bookingService.getAllByOwner(1L, State.ALL).size());
        assertEquals(0, bookingService.getAllByOwner(1L, State.CURRENT).size());
        assertEquals(0, bookingService.getAllByOwner(1L, State.PAST).size());
        assertEquals(0, bookingService.getAllByOwner(1L, State.FUTURE).size());
        assertEquals(0, bookingService.getAllByOwner(1L, State.WAITING).size());
        assertEquals(0, bookingService.getAllByOwner(1L, State.REJECTED).size());
    }
}
