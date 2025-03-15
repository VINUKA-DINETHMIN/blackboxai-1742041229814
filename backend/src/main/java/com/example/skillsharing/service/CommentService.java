package com.example.skillsharing.service;

import com.example.skillsharing.dto.CommentDTO;
import com.example.skillsharing.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    
    CommentDTO createComment(Long postId, CommentDTO.CreateCommentDTO createCommentDTO);
    
    CommentDTO getCommentById(Long commentId);
    
    Page<CommentDTO> getPostComments(Long postId, Pageable pageable);
    
    Page<CommentDTO> getUserComments(Long userId, Pageable pageable);
    
    CommentDTO updateComment(Long commentId, CommentDTO.UpdateCommentDTO updateCommentDTO);
    
    void deleteComment(Long commentId);
    
    boolean canModifyComment(Comment comment);
    
    CommentDTO convertToDTO(Comment comment);
    
    Comment convertToEntity(CommentDTO commentDTO);
}
