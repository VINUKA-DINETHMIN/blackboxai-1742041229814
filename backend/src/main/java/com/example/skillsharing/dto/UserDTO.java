package com.example.skillsharing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 255, message = "Bio cannot exceed 255 characters")
    private String bio;

    private String profilePicture;

    // Statistics
    private int postsCount;
    private int followersCount;
    private int followingCount;

    // Additional fields for response
    private boolean isFollowing;
}
