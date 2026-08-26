package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

public final class CommentMapper {
    private CommentMapper() {
    }

    public static Comment toComment(CommentDto dto) {
        return Comment.builder()
                .id(dto.getId())
                .text(dto.getText())
                .created(dto.getCreated())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}
