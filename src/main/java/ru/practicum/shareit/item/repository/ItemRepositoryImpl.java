package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.utils.IdentityGenerator;

import java.util.*;

@Component
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public List<Item> getAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Optional<Item> getById(Long id) {
        return items.get(id) != null ? Optional.of(items.get(id)) : Optional.empty();
    }

    @Override
    public Item create(Item item) {

        if (Objects.isNull(item.getId())) {
            item.setId(IdentityGenerator.INSTANCE.generateId(Item.class));
        }

        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public void delete(Long id) {
        items.remove(id);
    }
}
