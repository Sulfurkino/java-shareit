package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private static final Sort START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public BookingDto create(BookingShortDto dto, Long userId) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id: " + userId));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с id: " + dto.getItemId()));
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нельзя бронировать собственную вещь");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new BadRequestException("Окончание бронирования должно быть позже начала");
        }

        Booking booking = BookingMapper.toBooking(dto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, Long userId, Boolean approved) {
        Booking booking = findBooking(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Подтвердить бронирование может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Бронирование уже подтверждено или отклонено");
        }
        booking.setStatus(Boolean.TRUE.equals(approved) ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = findBooking(bookingId);
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new NotFoundException("Бронирование доступно только автору и владельцу вещи");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getAllByBooker(Long userId, State state) {
        requireUser(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBookerId(userId, START_DESC);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(
                        userId, now, now, START_DESC);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndBefore(userId, now, START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartAfter(userId, now, START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatus(
                        userId, BookingStatus.WAITING, START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatus(
                        userId, BookingStatus.REJECTED, START_DESC);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }
        return toDtos(bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getAllByOwner(Long userId, State state) {
        requireUser(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByItemOwnerId(userId, START_DESC);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(
                        userId, now, now, START_DESC);
                break;
            case PAST:
                bookings = bookingRepository.findAllByItemOwnerIdAndEndBefore(userId, now, START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartAfter(userId, now, START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatus(
                        userId, BookingStatus.WAITING, START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatus(
                        userId, BookingStatus.REJECTED, START_DESC);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }
        return toDtos(bookings);
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Не найдено бронирование с id: " + bookingId));
    }

    private void requireUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Не найден пользователь с id: " + userId);
        }
    }

    private List<BookingDto> toDtos(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}
