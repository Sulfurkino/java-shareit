package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemService itemService;

    @Test
    void shouldCreateItemWithRequestId() throws Exception {
        when(itemService.create(any(), eq(2L))).thenReturn(
                ItemDto.builder().id(1L).name("Drill").description("x").available(true).requestId(9L).build());
        mockMvc.perform(post("/items").header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"x\",\"available\":true,\"requestId\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(9));
    }

    @Test
    void shouldGetAllItems() throws Exception {
        when(itemService.getAll(2L)).thenReturn(List.of(
                ItemDto.builder().id(1L).name("Drill").description("x").available(true).build()));

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldGetItemById() throws Exception {
        when(itemService.getById(1L, 2L)).thenReturn(
                ItemDto.builder().id(1L).name("Drill").description("x").available(true).build());

        mockMvc.perform(get("/items/1").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        when(itemService.update(any(), eq(1L), eq(2L))).thenReturn(
                ItemDto.builder().id(1L).name("Updated").description("x").available(false).build());

        mockMvc.perform(patch("/items/1").header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldDeleteItem() throws Exception {
        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk());
        verify(itemService).delete(1L);
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemService.search("drill")).thenReturn(List.of(
                ItemDto.builder().id(1L).name("Drill").description("x").available(true).build()));

        mockMvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void shouldCreateComment() throws Exception {
        when(itemService.createComment(eq(1L), eq(2L), any())).thenReturn(
                CommentDto.builder().id(3L).text("great").authorName("George")
                        .created(LocalDateTime.now()).build());

        mockMvc.perform(post("/items/1/comment").header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CommentDto.builder().text("great").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.text").value("great"));
    }
}
