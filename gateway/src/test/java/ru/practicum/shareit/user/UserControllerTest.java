package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(ErrorHandler.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserClient userClient;

    @Test
    void shouldCreateUser() throws Exception {
        when(userClient.create(any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "name", "George", "email", "g@ex.com")));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"George\",\"email\":\"g@ex.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        verify(userClient).create(any());
    }

    @Test
    void shouldRejectUserWithoutEmail() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"George\"}"))
                .andExpect(status().isBadRequest());
        verify(userClient, never()).create(any());
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"George\",\"email\":\"invalid\"}"))
                .andExpect(status().isBadRequest());
        verify(userClient, never()).create(any());
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        when(userClient.getAll()).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
        verify(userClient).getAll();
    }

    @Test
    void shouldGetUserById() throws Exception {
        when(userClient.getById(1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        verify(userClient).getById(1L);
    }

    @Test
    void shouldUpdateUser() throws Exception {
        when(userClient.update(any(), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@ex.com\"}"))
                .andExpect(status().isOk());
        verify(userClient).update(any(), any());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        when(userClient.delete(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
        verify(userClient).delete(1L);
    }
}
