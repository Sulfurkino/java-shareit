package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.IdentityGenerator;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();

    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> getById(Long id) {
        return users.get(id) == null ? Optional.empty() : Optional.of(users.get(id));
    }

    public User save(User user) {
        if (Objects.isNull(user.getId())) {
            user.setId(IdentityGenerator.INSTANCE.generateId(User.class));
        }

        users.put(user.getId(), user);
        return user;
    }

    public void delete(Long id) {
        users.remove(id);
    }
}
