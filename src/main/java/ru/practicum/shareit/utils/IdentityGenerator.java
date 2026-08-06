package ru.practicum.shareit.utils;

import java.util.HashMap;
import java.util.Map;

public enum IdentityGenerator {
    INSTANCE;
    private Map<Class<?>, Long> idMap = new HashMap<>();

    public Long generateId(Class<?> clazz) {
        long id;

        if (idMap.containsKey(clazz)) {
            id = idMap.get(clazz) + 1;
            idMap.put(clazz,id);
        } else {
            idMap.put(clazz,id = 1L);
        }
        return id;
    }
}
