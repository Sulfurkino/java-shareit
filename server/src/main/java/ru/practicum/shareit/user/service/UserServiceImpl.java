package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw duplicateEmail(userDTO.getEmail());
        }
        return UserMapper.toDTO(userRepository.save(UserMapper.toEntity(userDTO)));
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDTO) {
        User user = getById(id);
        if (userDTO.getEmail() != null && userRepository.existsByEmailAndIdNot(userDTO.getEmail(), id)) {
            throw duplicateEmail(userDTO.getEmail());
        }
        Optional.ofNullable(userDTO.getName()).ifPresent(user::setName);
        Optional.ofNullable(userDTO.getEmail()).ifPresent(user::setEmail);
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = getById(id);
        userRepository.delete(user);
    }

    private DuplicateEmailException duplicateEmail(String email) {
        return new DuplicateEmailException("Пользователь с почтой " + email + " уже существует");
    }
}
