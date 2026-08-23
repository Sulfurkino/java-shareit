package ru.practicum.shareit.request.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private static final Sort CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository,
                                  UserRepository userRepository,
                                  ItemRepository itemRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id " + userId));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(user);
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getAllByUser(Long userId) {
        requireUser(userId);
        List<ItemRequestDto> requests = itemRequestRepository
                .findAllByRequestorId(userId, CREATED_DESC).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        requests.forEach(this::setItemsToItemRequestDto);
        return requests;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getAll(int from, int size, Long userId) {
        requireUser(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size, CREATED_DESC);
        List<ItemRequestDto> requests = itemRequestRepository
                .findAllByRequestorIdNot(userId, pageRequest).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        requests.forEach(this::setItemsToItemRequestDto);
        return requests;
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequestDto getById(Long requestId, Long userId) {
        requireUser(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Не существует запроса с id " + requestId));
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        setItemsToItemRequestDto(itemRequestDto);
        return itemRequestDto;
    }

    private void setItemsToItemRequestDto(ItemRequestDto itemRequestDto) {
        itemRequestDto.setItems(itemRepository.findAllByRequestId(itemRequestDto.getId()).stream()
                .map(ItemMapper::toItemShortDto)
                .collect(Collectors.toList()));
    }

    private void requireUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Не найден пользователь с id " + userId);
        }
    }
}
