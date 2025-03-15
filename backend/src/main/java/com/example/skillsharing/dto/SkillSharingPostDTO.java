package com.example.skillsharing.dto;

import com.example.skillsharing.model.SkillSharingPost.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SkillSharingPostDTO {
    private Long id;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 3, message = "Maximum of 3 media files allowed")
    private List<String> mediaUrls = new ArrayList<>();

    private MediaType mediaType;

    private UserDTO user;

    private int likesCount;
    private int commentsCount;
    private boolean isLiked;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For creating/updating posts
    public static class CreatePostDTO {
        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        private String description;

        private MediaType mediaType;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        public void setMediaType(MediaType mediaType) {
            this.mediaType = mediaType;
        }
    }

    // For updating post description
    public static class UpdatePostDTO {
        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
