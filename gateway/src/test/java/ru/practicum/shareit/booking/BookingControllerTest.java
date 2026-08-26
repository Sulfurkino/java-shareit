package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ErrorHandler;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    @MockBean
    private BookingClient bookingClient;

    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingClient.create(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "status", "WAITING")));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":5,\"start\":\"2030-08-22T10:00:00\",\"end\":\"2030-08-23T10:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        verify(bookingClient).create(eq(1L), any());
    }

    @Test
    void shouldRejectBookingWithoutStartEndItemId() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verify(bookingClient, never()).create(any(), any());
    }

    @Test
    void shouldRejectUnknownState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: UNKNOWN"));
        verify(bookingClient, never()).getAllByBooker(any(), any());
    }

    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingClient.approve(2L, 4L, true))
                .thenReturn(ResponseEntity.ok(Map.of("id", 4, "status", "APPROVED")));

        mockMvc.perform(patch("/bookings/4")
                        .header("X-Sharer-User-Id", 2)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        verify(bookingClient).approve(2L, 4L, true);
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingClient.getById(1L, 4L)).thenReturn(ResponseEntity.ok(Map.of("id", 4)));

        mockMvc.perform(get("/bookings/4").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
        verify(bookingClient).getById(1L, 4L);
    }

    @Test
    void shouldGetBookingsByBooker() throws Exception {
        when(bookingClient.getAllByBooker(1L, BookingState.ALL)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", 1).param("state", "ALL"))
                .andExpect(status().isOk());
        verify(bookingClient).getAllByBooker(1L, BookingState.ALL);
    }

    @Test
    void shouldGetBookingsByOwner() throws Exception {
        when(bookingClient.getAllByOwner(2L, BookingState.WAITING)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 2).param("state", "WAITING"))
                .andExpect(status().isOk());
        verify(bookingClient).getAllByOwner(2L, BookingState.WAITING);
    }
}
