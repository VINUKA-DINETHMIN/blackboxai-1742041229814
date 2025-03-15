package com.example.skillsharing.service.impl;

import com.example.skillsharing.dto.SkillSharingPostDTO;
import com.example.skillsharing.exception.BadRequestException;
import com.example.skillsharing.exception.ResourceNotFoundException;
import com.example.skillsharing.model.SkillSharingPost;
import com.example.skillsharing.model.User;
import com.example.skillsharing.repository.SkillSharingPostRepository;
import com.example.skillsharing.service.SkillSharingPostService;
import com.example.skillsharing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillSharingPostServiceImpl implements SkillSharingPostService {

    private final SkillSharingPostRepository postRepository;
    private final UserService userService;
    private static final String UPLOAD_DIR = "uploads/posts";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public SkillSharingPostDTO createPost(SkillSharingPostDTO.CreatePostDTO createPostDTO, List<MultipartFile> mediaFiles) {
        validateMediaFiles(mediaFiles);

        User currentUser = userService.getCurrentUser();
        SkillSharingPost post = new SkillSharingPost();
        post.setUser(currentUser);
        post.setDescription(createPostDTO.getDescription());
        post.setMediaType(createPostDTO.getMediaType());

        // Handle media file uploads
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            List<String> mediaUrls = new ArrayList<>();
            for (MultipartFile file : mediaFiles) {
                String fileName = saveMediaFile(file);
                mediaUrls.add(fileName);
            }
            post.setMediaUrls(mediaUrls);
        }

        SkillSharingPost savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public SkillSharingPostDTO getPostById(Long postId) {
        SkillSharingPost post = getPostEntityById(postId);
        return convertToDTO(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkillSharingPostDTO> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkillSharingPostDTO> getUserPosts(Long userId, Pageable pageable) {
        User user = userService.getUserEntityById(userId);
        return postRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkillSharingPostDTO> getFeedPosts(Long userId, Pageable pageable) {
        User user = userService.getUserEntityById(userId);
        return postRepository.findFeedPostsByUser(user, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public SkillSharingPostDTO updatePost(Long postId, SkillSharingPostDTO.UpdatePostDTO updatePostDTO) {
        SkillSharingPost post = getPostEntityById(postId);
        validatePostOwnership(post);

        post.setDescription(updatePostDTO.getDescription());
        SkillSharingPost updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }

    @Override
    public void deletePost(Long postId) {
        SkillSharingPost post = getPostEntityById(postId);
        validatePostOwnership(post);

        // Delete associated media files
        for (String mediaUrl : post.getMediaUrls()) {
            deleteMediaFile(mediaUrl);
        }

        postRepository.delete(post);
    }

    @Override
    public SkillSharingPostDTO likePost(Long postId) {
        SkillSharingPost post = getPostEntityById(postId);
        User currentUser = userService.getCurrentUser();

        if (post.getLikes().contains(currentUser)) {
            throw new BadRequestException("Post is already liked by the user");
        }

        post.getLikes().add(currentUser);
        SkillSharingPost updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }

    @Override
    public SkillSharingPostDTO unlikePost(Long postId) {
        SkillSharingPost post = getPostEntityById(postId);
        User currentUser = userService.getCurrentUser();

        if (!post.getLikes().contains(currentUser)) {
            throw new BadRequestException("Post is not liked by the user");
        }

        post.getLikes().remove(currentUser);
        SkillSharingPost updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillSharingPostDTO> getTrendingPosts() {
        return postRepository.findTop10ByOrderByLikesDesc().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPostLikedByUser(Long postId, Long userId) {
        SkillSharingPost post = getPostEntityById(postId);
        User user = userService.getUserEntityById(userId);
        return postRepository.isLikedByUser(post, user);
    }

    @Override
    public void validateMediaFiles(List<MultipartFile> mediaFiles) {
        if (mediaFiles == null || mediaFiles.isEmpty()) {
            return;
        }

        if (mediaFiles.size() > 3) {
            throw new BadRequestException("Maximum 3 media files allowed per post");
        }

        for (MultipartFile file : mediaFiles) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new BadRequestException("File size exceeds maximum limit of 10MB");
            }

            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
                throw new BadRequestException("Invalid file type. Only images and videos are allowed");
            }
        }
    }

    @Override
    public SkillSharingPostDTO convertToDTO(SkillSharingPost post) {
        SkillSharingPostDTO dto = new SkillSharingPostDTO();
        dto.setId(post.getId());
        dto.setDescription(post.getDescription());
        dto.setMediaUrls(post.getMediaUrls());
        dto.setMediaType(post.getMediaType());
        dto.setUser(userService.convertToDTO(post.getUser()));
        dto.setLikesCount(post.getLikes().size());
        dto.setCommentsCount(post.getComments().size());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());

        // Set isLiked if there's a current user
        try {
            User currentUser = userService.getCurrentUser();
            dto.setLiked(post.getLikes().contains(currentUser));
        } catch (Exception e) {
            dto.setLiked(false);
        }

        return dto;
    }

    @Override
    public SkillSharingPost convertToEntity(SkillSharingPostDTO postDTO) {
        SkillSharingPost post = new SkillSharingPost();
        post.setDescription(postDTO.getDescription());
        post.setMediaUrls(postDTO.getMediaUrls());
        post.setMediaType(postDTO.getMediaType());
        return post;
    }

    private SkillSharingPost getPostEntityById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
    }

    private void validatePostOwnership(SkillSharingPost post) {
        User currentUser = userService.getCurrentUser();
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't have permission to modify this post");
        }
    }

    private String saveMediaFile(MultipartFile file) {
        try {
            // Create uploads directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            return filename;
        } catch (IOException e) {
            throw new BadRequestException("Failed to save media file: " + e.getMessage());
        }
    }

    private void deleteMediaFile(String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't throw exception as the post should still be deleted
            System.err.println("Failed to delete media file: " + filename);
        }
    }
}
