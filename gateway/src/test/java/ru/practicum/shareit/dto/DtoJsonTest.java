package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class DtoJsonTest {
    @Autowired
    private JacksonTester<BookingShortDto> json;

    @Test
    void shouldDeserializeBookingDates() throws Exception {
        String body = "{\"itemId\":1,\"start\":\"2026-08-22T10:00:00\",\"end\":\"2026-08-23T10:00:00\"}";
        BookingShortDto dto = json.parseObject(body);
        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 8, 22, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 8, 23, 10, 0));
    }
}
