package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> getAll();

    Optional<User> getById(Long id);

    User save(User user);

    void delete(Long id);
}
