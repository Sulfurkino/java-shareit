package ru.practicum.shareit.item;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemClient itemClient;

    @Test
    void shouldCreateItem() throws Exception {
        when(itemClient.create(eq(2L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "name", "Drill", "requestId", 9)));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"x\",\"available\":true,\"requestId\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(9));
        verify(itemClient).create(eq(2L), any());
    }

    @Test
    void shouldRejectItemWithoutRequiredFields() throws Exception {
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verify(itemClient, never()).create(any(), any());
    }

    @Test
    void shouldGetAllItems() throws Exception {
        when(itemClient.getAll(2L)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk());
        verify(itemClient).getAll(2L);
    }

    @Test
    void shouldGetItemById() throws Exception {
        when(itemClient.getById(2L, 1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/items/1").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        verify(itemClient).getById(2L, 1L);
    }

    @Test
    void shouldUpdateItem() throws Exception {
        when(itemClient.update(eq(2L), eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk());
        verify(itemClient).update(eq(2L), eq(1L), any());
    }

    @Test
    void shouldDeleteItem() throws Exception {
        when(itemClient.delete(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk());
        verify(itemClient).delete(1L);
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemClient.search("drill")).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk());
        verify(itemClient).search("drill");
    }

    @Test
    void shouldCreateComment() throws Exception {
        when(itemClient.createComment(eq(2L), eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 3, "text", "great")));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"great\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("great"));
        verify(itemClient).createComment(eq(2L), eq(1L), any());
    }
}
