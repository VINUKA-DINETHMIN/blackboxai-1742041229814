package com.example.skillsharing.service;

import com.example.skillsharing.dto.SkillSharingPostDTO;
import com.example.skillsharing.model.SkillSharingPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SkillSharingPostService {
    
    SkillSharingPostDTO createPost(SkillSharingPostDTO.CreatePostDTO createPostDTO, List<MultipartFile> mediaFiles);
    
    SkillSharingPostDTO getPostById(Long postId);
    
    Page<SkillSharingPostDTO> getAllPosts(Pageable pageable);
    
    Page<SkillSharingPostDTO> getUserPosts(Long userId, Pageable pageable);
    
    Page<SkillSharingPostDTO> getFeedPosts(Long userId, Pageable pageable);
    
    SkillSharingPostDTO updatePost(Long postId, SkillSharingPostDTO.UpdatePostDTO updatePostDTO);
    
    void deletePost(Long postId);
    
    SkillSharingPostDTO likePost(Long postId);
    
    SkillSharingPostDTO unlikePost(Long postId);
    
    List<SkillSharingPostDTO> getTrendingPosts();
    
    boolean isPostLikedByUser(Long postId, Long userId);
    
    void validateMediaFiles(List<MultipartFile> mediaFiles);
    
    SkillSharingPostDTO convertToDTO(SkillSharingPost post);
    
    SkillSharingPost convertToEntity(SkillSharingPostDTO postDTO);
}
