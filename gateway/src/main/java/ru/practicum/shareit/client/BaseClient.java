package ru.practicum.shareit.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path, Long userId) {
        return get(path, userId, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId, @Nullable Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(String path, Long userId, T body) {
        return post(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, Long userId,
                                              @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId, T body) {
        return patch(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId,
                                               @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> delete(String path, Long userId) {
        return makeAndSendRequest(HttpMethod.DELETE, path, userId, null, null);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, Long userId,
                                                          @Nullable Map<String, Object> parameters,
                                                          @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));
        try {
            ResponseEntity<Object> response = parameters != null
                    ? rest.exchange(path, method, requestEntity, Object.class, parameters)
                    : rest.exchange(path, method, requestEntity, Object.class);
            return prepareGatewayResponse(response);
        } catch (HttpStatusCodeException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getResponseBodyAsByteArray());
        }
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.getStatusCode());
        return response.hasBody() ? builder.body(response.getBody()) : builder.build();
    }
}
