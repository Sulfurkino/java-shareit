package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.State;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingShortDto dto, Long userId);

    BookingDto approve(Long bookingId, Long userId, Boolean approved);

    BookingDto getById(Long bookingId, Long userId);

    List<BookingDto> getAllByBooker(Long userId, State state);

    List<BookingDto> getAllByOwner(Long userId, State state);
}
