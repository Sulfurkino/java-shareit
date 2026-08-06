package ru.practicum.shareit.user;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserServiceImpl userServiceImpl;

    public UserController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable Long id) {
        return UserMapper.toDTO(userServiceImpl.getById(id));
    }

    @GetMapping
    public List<UserDTO> getAll() {
        return userServiceImpl.getAll();
    }

    @PostMapping
    public UserDTO create(@Valid @RequestBody UserDTO userDTO) {
        return userServiceImpl.create(userDTO);
    }

    @PatchMapping("/{id}")
    public UserDTO update(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return userServiceImpl.update(id, userDTO);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userServiceImpl.delete(id);
    }
}
