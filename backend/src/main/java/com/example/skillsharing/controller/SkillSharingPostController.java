package com.example.skillsharing.controller;

import com.example.skillsharing.dto.SkillSharingPostDTO;
import com.example.skillsharing.security.CurrentUser;
import com.example.skillsharing.security.UserPrincipal;
import com.example.skillsharing.service.SkillSharingPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class SkillSharingPostController {

    private final SkillSharingPostService postService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public SkillSharingPostDTO createPost(@CurrentUser UserPrincipal currentUser,
                                        @Valid @RequestPart("post") SkillSharingPostDTO.CreatePostDTO createPostDTO,
                                        @RequestPart(value = "media", required = false) List<MultipartFile> mediaFiles) {
        return postService.createPost(createPostDTO, mediaFiles);
    }

    @GetMapping("/{postId}")
    public SkillSharingPostDTO getPost(@PathVariable Long postId) {
        return postService.getPostById(postId);
    }

    @GetMapping
    public Page<SkillSharingPostDTO> getAllPosts(Pageable pageable) {
        return postService.getAllPosts(pageable);
    }

    @GetMapping("/user/{userId}")
    public Page<SkillSharingPostDTO> getUserPosts(@PathVariable Long userId, Pageable pageable) {
        return postService.getUserPosts(userId, pageable);
    }

    @GetMapping("/feed")
    @PreAuthorize("hasRole('USER')")
    public Page<SkillSharingPostDTO> getFeedPosts(@CurrentUser UserPrincipal currentUser, Pageable pageable) {
        return postService.getFeedPosts(currentUser.getId(), pageable);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('USER')")
    public SkillSharingPostDTO updatePost(@PathVariable Long postId,
                                        @Valid @RequestBody SkillSharingPostDTO.UpdatePostDTO updatePostDTO) {
        return postService.updatePost(postId, updatePostDTO);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/like")
    @PreAuthorize("hasRole('USER')")
    public SkillSharingPostDTO likePost(@PathVariable Long postId) {
        return postService.likePost(postId);
    }

    @PostMapping("/{postId}/unlike")
    @PreAuthorize("hasRole('USER')")
    public SkillSharingPostDTO unlikePost(@PathVariable Long postId) {
        return postService.unlikePost(postId);
    }

    @GetMapping("/trending")
    public List<SkillSharingPostDTO> getTrendingPosts() {
        return postService.getTrendingPosts();
    }

    @GetMapping("/{postId}/is-liked")
    @PreAuthorize("hasRole('USER')")
    public boolean isPostLiked(@CurrentUser UserPrincipal currentUser,
                              @PathVariable Long postId) {
        return postService.isPostLikedByUser(postId, currentUser.getId());
    }
}
