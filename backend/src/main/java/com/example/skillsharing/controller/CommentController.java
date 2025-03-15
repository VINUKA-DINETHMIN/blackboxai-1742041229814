package com.example.skillsharing.controller;

import com.example.skillsharing.dto.CommentDTO;
import com.example.skillsharing.security.CurrentUser;
import com.example.skillsharing.security.UserPrincipal;
import com.example.skillsharing.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("hasRole('USER')")
    public CommentDTO createComment(@PathVariable Long postId,
                                  @Valid @RequestBody CommentDTO.CreateCommentDTO createCommentDTO) {
        return commentService.createComment(postId, createCommentDTO);
    }

    @GetMapping("/comments/{commentId}")
    public CommentDTO getComment(@PathVariable Long commentId) {
        return commentService.getCommentById(commentId);
    }

    @GetMapping("/posts/{postId}/comments")
    public Page<CommentDTO> getPostComments(@PathVariable Long postId, Pageable pageable) {
        return commentService.getPostComments(postId, pageable);
    }

    @GetMapping("/users/{userId}/comments")
    public Page<CommentDTO> getUserComments(@PathVariable Long userId, Pageable pageable) {
        return commentService.getUserComments(userId, pageable);
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public CommentDTO updateComment(@PathVariable Long commentId,
                                  @Valid @RequestBody CommentDTO.UpdateCommentDTO updateCommentDTO) {
        return commentService.updateComment(commentId, updateCommentDTO);
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteComment(@CurrentUser UserPrincipal currentUser,
                                         @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}
