package ru.practicum.shareit.user;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        return UserMapper.toDTO(userService.getById(id));
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDTO) {
        return userService.create(userDTO);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto userDTO) {
        return userService.update(id, userDTO);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
