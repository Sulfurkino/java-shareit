package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User getById(Long id);

    List<UserDto> getAll();

    UserDto create(UserDto userDTO);

    UserDto update(Long id, UserDto userDTO);

    void delete(Long id);
}
