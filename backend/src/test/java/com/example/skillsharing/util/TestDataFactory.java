package com.example.skillsharing.util;

import com.example.skillsharing.dto.*;
import com.example.skillsharing.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestDataFactory {

    public static User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPassword("password" + id);
        user.setBio("Bio for user " + id);
        user.setProfilePicture("profile" + id + ".jpg");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static List<User> createUsers(int count) {
        return IntStream.range(1, count + 1)
                .mapToObj(TestDataFactory::createUser)
                .collect(Collectors.toList());
    }

    public static SkillSharingPost createPost(Long id, User author) {
        SkillSharingPost post = new SkillSharingPost();
        post.setId(id);
        post.setTitle("Post " + id);
        post.setContent("Content for post " + id);
        post.setAuthor(author);
        post.setSkills(Arrays.asList("Skill1", "Skill2"));
        post.setMediaUrls(Arrays.asList("media1.jpg", "media2.jpg"));
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }

    public static List<SkillSharingPost> createPosts(int count, User author) {
        return IntStream.range(1, count + 1)
                .mapToObj(i -> createPost((long) i, author))
                .collect(Collectors.toList());
    }

    public static Comment createComment(Long id, User author, SkillSharingPost post) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent("Comment " + id + " content");
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        return comment;
    }

    public static List<Comment> createComments(int count, User author, SkillSharingPost post) {
        return IntStream.range(1, count + 1)
                .mapToObj(i -> createComment((long) i, author, post))
                .collect(Collectors.toList());
    }

    public static LearningPlan createLearningPlan(Long id, User user) {
        LearningPlan plan = new LearningPlan();
        plan.setId(id);
        plan.setTitle("Learning Plan " + id);
        plan.setDescription("Description for plan " + id);
        plan.setUser(user);
        plan.setStatus(LearningPlan.PlanStatus.IN_PROGRESS);
        plan.setSkills(Arrays.asList("Skill1", "Skill2"));
        plan.setResources(Arrays.asList("Resource1", "Resource2"));
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        return plan;
    }

    public static List<LearningPlan> createLearningPlans(int count, User user) {
        return IntStream.range(1, count + 1)
                .mapToObj(i -> createLearningPlan((long) i, user))
                .collect(Collectors.toList());
    }

    public static Notification createNotification(Long id, User user) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUser(user);
        notification.setTitle("Notification " + id);
        notification.setMessage("Message for notification " + id);
        notification.setType(Notification.NotificationType.COMMENT);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    public static List<Notification> createNotifications(int count, User user) {
        return IntStream.range(1, count + 1)
                .mapToObj(i -> createNotification((long) i, user))
                .collect(Collectors.toList());
    }

    public static UserDTO createUserDTO(Long id) {
        UserDTO dto = new UserDTO();
        dto.setId(id);
        dto.setUsername("user" + id);
        dto.setEmail("user" + id + "@example.com");
        dto.setBio("Bio for user " + id);
        dto.setProfilePicture("profile" + id + ".jpg");
        return dto;
    }

    public static SkillSharingPostDTO createPostDTO(Long id) {
        SkillSharingPostDTO dto = new SkillSharingPostDTO();
        dto.setId(id);
        dto.setTitle("Post " + id);
        dto.setContent("Content for post " + id);
        dto.setAuthorId(1L);
        dto.setSkills(Arrays.asList("Skill1", "Skill2"));
        dto.setMediaUrls(Arrays.asList("media1.jpg", "media2.jpg"));
        return dto;
    }

    public static CommentDTO createCommentDTO(Long id) {
        CommentDTO dto = new CommentDTO();
        dto.setId(id);
        dto.setContent("Comment " + id + " content");
        dto.setAuthorId(1L);
        dto.setPostId(1L);
        return dto;
    }

    public static LearningPlanDTO createLearningPlanDTO(Long id) {
        LearningPlanDTO dto = new LearningPlanDTO();
        dto.setId(id);
        dto.setTitle("Learning Plan " + id);
        dto.setDescription("Description for plan " + id);
        dto.setUserId(1L);
        dto.setStatus(LearningPlan.PlanStatus.IN_PROGRESS);
        dto.setSkills(Arrays.asList("Skill1", "Skill2"));
        dto.setResources(Arrays.asList("Resource1", "Resource2"));
        return dto;
    }

    public static NotificationDTO createNotificationDTO(Long id) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(id);
        dto.setUserId(1L);
        dto.setTitle("Notification " + id);
        dto.setMessage("Message for notification " + id);
        dto.setType(Notification.NotificationType.COMMENT);
        dto.setRead(false);
        return dto;
    }

    public static <T> Page<T> createPage(List<T> content, int pageNumber, int pageSize, long totalElements) {
        return new PageImpl<>(
            content,
            PageRequest.of(pageNumber, pageSize),
            totalElements
        );
    }

    public static SignUpRequest createSignUpRequest() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setBio("New user bio");
        return request;
    }

    public static LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");
        return request;
    }
}
