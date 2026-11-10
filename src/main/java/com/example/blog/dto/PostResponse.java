package com.example.blog.dto;

import com.example.blog.entity.Post;
import com.example.blog.entity.PostStatus;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String slug,
        String content,
        PostStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post p) {
        return new PostResponse(p.getId(), p.getTitle(), p.getSlug(), p.getContent(),
                p.getStatus(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
