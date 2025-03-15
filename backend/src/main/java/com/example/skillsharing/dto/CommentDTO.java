package com.example.skillsharing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;

    @NotBlank(message = "Comment content is required")
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String content;

    private UserDTO user;
    private Long postId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean edited;
    private boolean canEdit;
    private boolean canDelete;

    // For creating/updating comments
    public static class CreateCommentDTO {
        @NotBlank(message = "Comment content is required")
        @Size(max = 500, message = "Comment cannot exceed 500 characters")
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    // For updating comments
    public static class UpdateCommentDTO {
        @NotBlank(message = "Comment content is required")
        @Size(max = 500, message = "Comment cannot exceed 500 characters")
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
