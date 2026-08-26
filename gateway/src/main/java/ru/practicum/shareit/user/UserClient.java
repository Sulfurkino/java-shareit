package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Component
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .build());
    }

    public ResponseEntity<Object> getAll() {
        return get("", null);
    }

    public ResponseEntity<Object> getById(Long id) {
        return get("/" + id, null);
    }

    public ResponseEntity<Object> create(UserDto dto) {
        return post("", null, dto);
    }

    public ResponseEntity<Object> update(Long id, UserDto dto) {
        return patch("/" + id, null, dto);
    }

    public ResponseEntity<Object> delete(Long id) {
        return delete("/" + id, null);
    }
}
