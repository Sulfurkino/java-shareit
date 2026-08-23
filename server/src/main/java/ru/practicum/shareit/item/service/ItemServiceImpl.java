package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository,
                           ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAll(Long userId) {
        requireUser(userId);
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(item -> enrich(ItemMapper.toItemDto(item), true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getById(Long id, Long userId) {
        Item item = findItem(id);
        boolean showBookings = item.getOwner().getId().equals(userId);
        return enrich(ItemMapper.toItemDto(item), showBookings);
    }

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id: " + userId));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException(
                            "Не найден запрос с id: " + itemDto.getRequestId()));
            item.setRequest(itemRequest);
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, Long id, Long userId) {
        Item item = findItem(id);
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("У пользователя с id " + userId + " нет вещи с id " + id);
        }
        Optional.ofNullable(itemDto.getName()).ifPresent(item::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(item::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(item::setAvailable);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Item item = findItem(id);
        itemRepository.delete(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository
                .findAllByAvailableTrueAndNameContainingIgnoreCaseOrAvailableTrueAndDescriptionContainingIgnoreCase(
                        text, text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(Long itemId, Long userId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id: " + userId));
        Item item = findItem(itemId);
        boolean completedBooking = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!completedBooking) {
            throw new BadRequestException("Комментировать можно только после завершённого бронирования");
        }
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private ItemDto enrich(ItemDto dto, boolean showBookings) {
        dto.setComments(commentRepository.findAllByItemIdOrderByCreatedAsc(dto.getId()).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));
        if (showBookings) {
            LocalDateTime now = LocalDateTime.now();
            bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                            dto.getId(), BookingStatus.APPROVED, now)
                    .map(BookingMapper::toBookingShortDto)
                    .ifPresent(dto::setLastBooking);
            bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                            dto.getId(), BookingStatus.APPROVED, now)
                    .map(BookingMapper::toBookingShortDto)
                    .ifPresent(dto::setNextBooking);
        }
        return dto;
    }

    private Item findItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с id: " + itemId));
    }

    private void requireUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Не найден пользователь с id: " + userId);
        }
    }
}
