package ru.practicum.shareit.request;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@Import(ErrorHandler.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void shouldRejectBlankDescription() throws Exception {
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"\"}"))
                .andExpect(status().isBadRequest());
        verify(itemRequestClient, never()).create(any(), any());
    }

    @Test
    void shouldRejectInvalidPagination() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
        verify(itemRequestClient, never()).getAll(any(), any(Integer.class), any(Integer.class));
    }

    @Test
    void shouldCreateRequest() throws Exception {
        when(itemRequestClient.create(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "description", "Need a drill")));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Need a drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        verify(itemRequestClient).create(eq(1L), any());
    }

    @Test
    void shouldGetOwnRequests() throws Exception {
        when(itemRequestClient.getAllByUser(1L)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
        verify(itemRequestClient).getAllByUser(1L);
    }

    @Test
    void shouldGetAllRequests() throws Exception {
        when(itemRequestClient.getAll(1L, 0, 10)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
        verify(itemRequestClient).getAll(1L, 0, 10);
    }

    @Test
    void shouldGetRequestById() throws Exception {
        when(itemRequestClient.getById(1L, 5L))
                .thenReturn(ResponseEntity.ok(Map.of("id", 5, "description", "Need a drill")));

        mockMvc.perform(get("/requests/5").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
        verify(itemRequestClient).getById(1L, 5L);
    }
}
