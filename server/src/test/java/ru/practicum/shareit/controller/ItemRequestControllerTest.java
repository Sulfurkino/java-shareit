package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L).description("Need a drill").created(LocalDateTime.now()).items(List.of()).build();
        when(itemRequestService.create(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(ItemRequestDto.builder().description("Need a drill").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    void shouldGetOwnRequests() throws Exception {
        when(itemRequestService.getAllByUser(1L)).thenReturn(List.of());
        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAllRequests() throws Exception {
        when(itemRequestService.getAll(0, 10, 1L)).thenReturn(List.of());
        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetRequestById() throws Exception {
        when(itemRequestService.getById(5L, 1L)).thenReturn(
                ItemRequestDto.builder().id(5L).description("Need a drill").items(List.of()).build());
        mockMvc.perform(get("/requests/5").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }
}
