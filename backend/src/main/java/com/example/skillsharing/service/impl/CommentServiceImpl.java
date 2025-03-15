package com.example.skillsharing.service.impl;

import com.example.skillsharing.dto.CommentDTO;
import com.example.skillsharing.exception.BadRequestException;
import com.example.skillsharing.exception.ResourceNotFoundException;
import com.example.skillsharing.model.Comment;
import com.example.skillsharing.model.SkillSharingPost;
import com.example.skillsharing.model.User;
import com.example.skillsharing.repository.CommentRepository;
import com.example.skillsharing.service.CommentService;
import com.example.skillsharing.service.SkillSharingPostService;
import com.example.skillsharing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final SkillSharingPostService postService;

    @Override
    public CommentDTO createComment(Long postId, CommentDTO.CreateCommentDTO createCommentDTO) {
        User currentUser = userService.getCurrentUser();
        SkillSharingPost post = postService.convertToEntity(postService.getPostById(postId));

        Comment comment = new Comment();
        comment.setContent(createCommentDTO.getContent());
        comment.setUser(currentUser);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);
        return convertToDTO(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDTO getCommentById(Long commentId) {
        Comment comment = getCommentEntityById(commentId);
        return convertToDTO(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDTO> getPostComments(Long postId, Pageable pageable) {
        SkillSharingPost post = postService.convertToEntity(postService.getPostById(postId));
        return commentRepository.findByPostOrderByCreatedAtDesc(post, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDTO> getUserComments(Long userId, Pageable pageable) {
        User user = userService.getUserEntityById(userId);
        return commentRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .reduce(Page::empty, (a, b) -> a); // Convert List to Page
    }

    @Override
    public CommentDTO updateComment(Long commentId, CommentDTO.UpdateCommentDTO updateCommentDTO) {
        Comment comment = getCommentEntityById(commentId);
        
        if (!canModifyComment(comment)) {
            throw new BadRequestException("You don't have permission to modify this comment");
        }

        comment.setContent(updateCommentDTO.getContent());
        comment.setEdited(true);

        Comment updatedComment = commentRepository.save(comment);
        return convertToDTO(updatedComment);
    }

    @Override
    public void deleteComment(Long commentId) {
        Comment comment = getCommentEntityById(commentId);
        
        if (!canModifyComment(comment)) {
            throw new BadRequestException("You don't have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Override
    public boolean canModifyComment(Comment comment) {
        User currentUser = userService.getCurrentUser();
        return comment.getUser().getId().equals(currentUser.getId()) || 
               comment.getPost().getUser().getId().equals(currentUser.getId());
    }

    @Override
    public CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setUser(userService.convertToDTO(comment.getUser()));
        dto.setPostId(comment.getPost().getId());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setEdited(comment.isEdited());

        // Set permissions
        try {
            User currentUser = userService.getCurrentUser();
            boolean isCommentOwner = comment.getUser().getId().equals(currentUser.getId());
            boolean isPostOwner = comment.getPost().getUser().getId().equals(currentUser.getId());
            
            dto.setCanEdit(isCommentOwner);
            dto.setCanDelete(isCommentOwner || isPostOwner);
        } catch (Exception e) {
            dto.setCanEdit(false);
            dto.setCanDelete(false);
        }

        return dto;
    }

    @Override
    public Comment convertToEntity(CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        return comment;
    }

    private Comment getCommentEntityById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
    }
}
