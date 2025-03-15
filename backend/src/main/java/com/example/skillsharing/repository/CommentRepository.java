package com.example.skillsharing.repository;

import com.example.skillsharing.model.Comment;
import com.example.skillsharing.model.SkillSharingPost;
import com.example.skillsharing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostOrderByCreatedAtDesc(SkillSharingPost post, Pageable pageable);
    
    List<Comment> findByUserOrderByCreatedAtDesc(User user);
    
    long countByPost(SkillSharingPost post);
    
    void deleteByPostAndUser(SkillSharingPost post, User user);
    
    boolean existsByPostAndUser(SkillSharingPost post, User user);
}
