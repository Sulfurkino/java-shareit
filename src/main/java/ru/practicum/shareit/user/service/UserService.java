package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User getById(Long id);

    List<UserDTO> getAll();

    UserDTO create(UserDTO userDTO);

    UserDTO update(Long id, UserDTO userDTO);

    void delete(Long id);
}
