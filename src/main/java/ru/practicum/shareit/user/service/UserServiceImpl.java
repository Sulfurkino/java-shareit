package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepositoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepositoryImpl userRepositoryImpl;

    public UserServiceImpl(UserRepositoryImpl userRepositoryImpl) {
        this.userRepositoryImpl = userRepositoryImpl;
    }

    public User getById(Long id) {
        return userRepositoryImpl.getById(id)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id: " + id));
    }

    public List<UserDTO> getAll() {
        List<User> users = userRepositoryImpl.getAll();
        List<UserDTO> result = new ArrayList<>();

        if (users.isEmpty()) {
            return result;
        }

        for (User userToDTO : users) {
            result.add(UserMapper.toDTO(userToDTO));
        }

        return result;
    }

    public UserDTO create(UserDTO userDTO) {
        User user = UserMapper.toEntity(userDTO);
        validateEmailUniqueness(user.getEmail());

        User createdUser = userRepositoryImpl.save(user);
        return UserMapper.toDTO(createdUser);
    }

    public UserDTO update(Long id, UserDTO userDTO) {
        User user = getById(id);

        if (Objects.nonNull(userDTO.getEmail()) && !user.getEmail().equals(userDTO.getEmail())) {
            validateEmailUniqueness(userDTO.getEmail());
        }

        user.setName(Objects.requireNonNullElse(userDTO.getName(), user.getName()));
        user.setEmail(Objects.requireNonNullElse(userDTO.getEmail(), user.getEmail()));
        User updatedUser = userRepositoryImpl.save(user);
        return UserMapper.toDTO(updatedUser);
    }

    public void delete(Long id) {
        userRepositoryImpl.delete(id);
    }

    private void validateEmailUniqueness(String email) {
        List<User> users = userRepositoryImpl.getAll();
        for (User user : users) {
            if (email.equals(user.getEmail())) {
                log.error("Пользователь с такой почтой " + email + " уже существует");
                throw new DuplicateEmailException("Пользователь с такой почтой " + email + " уже существует");
            }
        }
    }
}
