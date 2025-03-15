package com.example.skillsharing.controller;

import com.example.skillsharing.base.BaseTest;
import com.example.skillsharing.dto.CommentDTO;
import com.example.skillsharing.model.Comment;
import com.example.skillsharing.model.SkillSharingPost;
import com.example.skillsharing.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerTest extends BaseTest {

    private SkillSharingPost testPost;

    @BeforeEach
    void setUp() {
        super.setUp();
        testPost = TestDataFactory.createPost(null, testUser);
        testPost = postRepository.save(testPost);
    }

    @Test
    void createComment_WithValidData_ReturnsCreatedComment() throws Exception {
        // Arrange
        CommentDTO.CreateCommentDTO createCommentDTO = new CommentDTO.CreateCommentDTO();
        createCommentDTO.setContent("Test comment content");

        // Act
        ResultActions result = mockMvc.perform(post("/api/posts/{postId}/comments", testPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createCommentDTO))
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(createCommentDTO.getContent())))
                .andExpect(jsonPath("$.authorId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.postId", is(testPost.getId().intValue())));
    }

    @Test
    void getComment_WithValidId_ReturnsComment() throws Exception {
        // Arrange
        Comment comment = TestDataFactory.createComment(null, testUser, testPost);
        comment = commentRepository.save(comment);

        // Act
        ResultActions result = mockMvc.perform(get("/api/comments/{commentId}", comment.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comment.getId().intValue())))
                .andExpect(jsonPath("$.content", is(comment.getContent())))
                .andExpect(jsonPath("$.authorId", is(testUser.getId().intValue())));
    }

    @Test
    void getPostComments_ReturnsPaginatedComments() throws Exception {
        // Arrange
        List<Comment> comments = TestDataFactory.createComments(3, testUser, testPost);
        commentRepository.saveAll(comments);

        // Act
        ResultActions result = mockMvc.perform(get("/api/posts/{postId}/comments", testPost.getId())
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void getUserComments_ReturnsPaginatedUserComments() throws Exception {
        // Arrange
        List<Comment> comments = TestDataFactory.createComments(3, testUser, testPost);
        commentRepository.saveAll(comments);

        // Act
        ResultActions result = mockMvc.perform(get("/api/users/{userId}/comments", testUser.getId())
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void updateComment_WithValidData_ReturnsUpdatedComment() throws Exception {
        // Arrange
        Comment comment = TestDataFactory.createComment(null, testUser, testPost);
        comment = commentRepository.save(comment);

        CommentDTO.UpdateCommentDTO updateCommentDTO = new CommentDTO.UpdateCommentDTO();
        updateCommentDTO.setContent("Updated comment content");

        // Act
        ResultActions result = mockMvc.perform(put("/api/comments/{commentId}", comment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateCommentDTO))
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(updateCommentDTO.getContent())));

        Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertEquals(updateCommentDTO.getContent(), updatedComment.getContent());
    }

    @Test
    void deleteComment_WithValidId_DeletesComment() throws Exception {
        // Arrange
        Comment comment = TestDataFactory.createComment(null, testUser, testPost);
        comment = commentRepository.save(comment);

        // Act
        ResultActions result = mockMvc.perform(delete("/api/comments/{commentId}", comment.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertFalse(commentRepository.existsById(comment.getId()));
    }

    @Test
    void createComment_WithInvalidPostId_ReturnsNotFound() throws Exception {
        // Arrange
        CommentDTO.CreateCommentDTO createCommentDTO = new CommentDTO.CreateCommentDTO();
        createCommentDTO.setContent("Test comment content");

        // Act
        ResultActions result = mockMvc.perform(post("/api/posts/{postId}/comments", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createCommentDTO))
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isNotFound());
    }

    @Test
    void updateComment_WithUnauthorizedUser_ReturnsForbidden() throws Exception {
        // Arrange
        User otherUser = TestDataFactory.createUser(null);
        otherUser = userRepository.save(otherUser);
        
        Comment comment = TestDataFactory.createComment(null, otherUser, testPost);
        comment = commentRepository.save(comment);

        CommentDTO.UpdateCommentDTO updateCommentDTO = new CommentDTO.UpdateCommentDTO();
        updateCommentDTO.setContent("Updated comment content");

        // Act
        ResultActions result = mockMvc.perform(put("/api/comments/{commentId}", comment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateCommentDTO))
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isForbidden());
    }
}
