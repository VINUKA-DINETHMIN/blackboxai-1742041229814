package com.example.skillsharing.repository;

import com.example.skillsharing.model.LearningPlan;
import com.example.skillsharing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LearningPlanRepository extends JpaRepository<LearningPlan, Long> {
    Page<LearningPlan> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<LearningPlan> findByUserAndStatusOrderByStartDateAsc(User user, LearningPlan.PlanStatus status);
    
    @Query("SELECT p FROM LearningPlan p WHERE p.user = :user AND p.endDate > :now AND p.status = 'IN_PROGRESS'")
    List<LearningPlan> findActiveUserPlans(User user, LocalDateTime now);
    
    List<LearningPlan> findByTopicsContainingIgnoreCase(String topic);
    
    long countByUserAndStatus(User user, LearningPlan.PlanStatus status);
}
