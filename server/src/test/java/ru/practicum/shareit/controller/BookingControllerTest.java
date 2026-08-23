package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ErrorHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private BookingService bookingService;

    @Test
    void shouldCreateBooking() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 22, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 23, 10, 0);
        when(bookingService.create(any(), eq(1L))).thenReturn(
                BookingDto.builder().id(1L).start(start).end(end).status(BookingStatus.WAITING).build());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(BookingShortDto.builder()
                                .start(start).end(end).itemId(5L).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingService.approve(4L, 2L, true)).thenReturn(
                BookingDto.builder().id(4L).status(BookingStatus.APPROVED).build());

        mockMvc.perform(patch("/bookings/4")
                        .header("X-Sharer-User-Id", 2)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingService.getById(4L, 1L)).thenReturn(
                BookingDto.builder().id(4L).status(BookingStatus.WAITING).build());

        mockMvc.perform(get("/bookings/4").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void shouldGetBookingsByBooker() throws Exception {
        when(bookingService.getAllByBooker(1L, State.ALL)).thenReturn(List.of(
                BookingDto.builder().id(4L).status(BookingStatus.WAITING).build()));

        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", 1).param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4));
    }

    @Test
    void shouldGetBookingsByOwner() throws Exception {
        when(bookingService.getAllByOwner(2L, State.WAITING)).thenReturn(List.of(
                BookingDto.builder().id(4L).status(BookingStatus.WAITING).build()));

        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 2).param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4));
    }
}
