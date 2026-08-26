package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeRequestWithCreatedAndItems() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.of(2026, 8, 21, 15, 0, 0))
                .items(List.of())
                .build();

        var content = json.write(dto);
        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(content).extractingJsonPathStringValue("$.created").startsWith("2026-08-21T15:00:00");
        assertThat(content).extractingJsonPathArrayValue("$.items").isEmpty();
    }
}
