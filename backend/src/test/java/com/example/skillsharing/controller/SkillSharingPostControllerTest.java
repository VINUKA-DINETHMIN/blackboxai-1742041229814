package com.example.skillsharing.controller;

import com.example.skillsharing.base.BaseTest;
import com.example.skillsharing.dto.SkillSharingPostDTO;
import com.example.skillsharing.model.SkillSharingPost;
import com.example.skillsharing.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SkillSharingPostControllerTest extends BaseTest {

    @Test
    void createPost_WithValidData_ReturnsCreatedPost() throws Exception {
        // Arrange
        SkillSharingPostDTO.CreatePostDTO createPostDTO = new SkillSharingPostDTO.CreatePostDTO();
        createPostDTO.setTitle("Test Post");
        createPostDTO.setContent("Test content");
        createPostDTO.setSkills(Arrays.asList("Java", "Spring Boot"));

        MockMultipartFile postData = new MockMultipartFile(
            "post",
            "",
            "application/json",
            asJsonString(createPostDTO).getBytes()
        );

        MockMultipartFile mediaFile = new MockMultipartFile(
            "media",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        // Act
        ResultActions result = mockMvc.perform(multipart("/api/posts")
                .file(postData)
                .file(mediaFile)
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(createPostDTO.getTitle())))
                .andExpect(jsonPath("$.content", is(createPostDTO.getContent())))
                .andExpect(jsonPath("$.skills", containsInAnyOrder("Java", "Spring Boot")))
                .andExpect(jsonPath("$.mediaUrls", hasSize(1)));
    }

    @Test
    void getPost_WithValidId_ReturnsPost() throws Exception {
        // Arrange
        SkillSharingPost post = TestDataFactory.createPost(null, testUser);
        post = postRepository.save(post);

        // Act
        ResultActions result = mockMvc.perform(get("/api/posts/{postId}", post.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(post.getId().intValue())))
                .andExpect(jsonPath("$.title", is(post.getTitle())))
                .andExpect(jsonPath("$.content", is(post.getContent())));
    }

    @Test
    void getAllPosts_ReturnsPaginatedPosts() throws Exception {
        // Arrange
        List<SkillSharingPost> posts = TestDataFactory.createPosts(3, testUser);
        postRepository.saveAll(posts);

        // Act
        ResultActions result = mockMvc.perform(get("/api/posts")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void getUserPosts_ReturnsPaginatedUserPosts() throws Exception {
        // Arrange
        List<SkillSharingPost> posts = TestDataFactory.createPosts(3, testUser);
        postRepository.saveAll(posts);

        // Act
        ResultActions result = mockMvc.perform(get("/api/posts/user/{userId}", testUser.getId())
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void getFeedPosts_ReturnsPaginatedFeedPosts() throws Exception {
        // Arrange
        User followedUser = TestDataFactory.createUser(null);
        followedUser = userRepository.save(followedUser);
        testUser.getFollowing().add(followedUser);
        userRepository.save(testUser);

        List<SkillSharingPost> posts = TestDataFactory.createPosts(3, followedUser);
        postRepository.saveAll(posts);

        // Act
        ResultActions result = mockMvc.perform(get("/api/posts/feed")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void updatePost_WithValidData_ReturnsUpdatedPost() throws Exception {
        // Arrange
        SkillSharingPost post = TestDataFactory.createPost(null, testUser);
        post = postRepository.save(post);

        SkillSharingPostDTO.UpdatePostDTO updatePostDTO = new SkillSharingPostDTO.UpdatePostDTO();
        updatePostDTO.setTitle("Updated Title");
        updatePostDTO.setContent("Updated content");

        // Act
        ResultActions result = mockMvc.perform(put("/api/posts/{postId}", post.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updatePostDTO))
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(updatePostDTO.getTitle())))
                .andExpect(jsonPath("$.content", is(updatePostDTO.getContent())));
    }

    @Test
    void deletePost_WithValidId_DeletesPost() throws Exception {
        // Arrange
        SkillSharingPost post = TestDataFactory.createPost(null, testUser);
        post = postRepository.save(post);

        // Act
        ResultActions result = mockMvc.perform(delete("/api/posts/{postId}", post.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertFalse(postRepository.existsById(post.getId()));
    }

    @Test
    void likePost_WhenNotLiked_LikesPost() throws Exception {
        // Arrange
        SkillSharingPost post = TestDataFactory.createPost(null, testUser);
        post = postRepository.save(post);

        // Act
        ResultActions result = mockMvc.perform(post("/api/posts/{postId}/like", post.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertTrue(postRepository.findById(post.getId()).get().getLikedBy().contains(testUser));
    }

    @Test
    void unlikePost_WhenLiked_UnlikesPost() throws Exception {
        // Arrange
        SkillSharingPost post = TestDataFactory.createPost(null, testUser);
        post.getLikedBy().add(testUser);
        post = postRepository.save(post);

        // Act
        ResultActions result = mockMvc.perform(post("/api/posts/{postId}/unlike", post.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertFalse(postRepository.findById(post.getId()).get().getLikedBy().contains(testUser));
    }

    @Test
    void getTrendingPosts_ReturnsTrendingPosts() throws Exception {
        // Arrange
        List<SkillSharingPost> posts = TestDataFactory.createPosts(5, testUser);
        posts.forEach(post -> post.getLikedBy().add(testUser)); // Add likes to make them trending
        postRepository.saveAll(posts);

        // Act
        ResultActions result = mockMvc.perform(get("/api/posts/trending")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }
}
