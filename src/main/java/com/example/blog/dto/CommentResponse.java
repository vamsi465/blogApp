package com.example.blog.dto;

import com.example.blog.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        String author,
        Long postId,
        LocalDateTime createdAt
) {
    public static CommentResponse from(Comment c) {
        return new CommentResponse(c.getId(), c.getContent(),
                c.getAuthor().getUsername(), c.getPost().getId(), c.getCreatedAt());
    }
}
