package com.example.skillsharing.repository;

import com.example.skillsharing.model.SkillSharingPost;
import com.example.skillsharing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillSharingPostRepository extends JpaRepository<SkillSharingPost, Long> {
    Page<SkillSharingPost> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    Page<SkillSharingPost> findByUserInOrderByCreatedAtDesc(Iterable<User> users, Pageable pageable);
    
    @Query("SELECT p FROM SkillSharingPost p WHERE p.user IN " +
           "(SELECT f FROM User u JOIN u.following f WHERE u = :user) " +
           "ORDER BY p.createdAt DESC")
    Page<SkillSharingPost> findFeedPostsByUser(User user, Pageable pageable);
    
    List<SkillSharingPost> findTop10ByOrderByLikesDesc();
    
    @Query("SELECT COUNT(l) > 0 FROM SkillSharingPost p JOIN p.likes l WHERE p = :post AND l = :user")
    boolean isLikedByUser(SkillSharingPost post, User user);
}
