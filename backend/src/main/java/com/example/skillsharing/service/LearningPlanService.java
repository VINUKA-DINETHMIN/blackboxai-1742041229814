package com.example.skillsharing.service;

import com.example.skillsharing.dto.LearningPlanDTO;
import com.example.skillsharing.model.LearningPlan;
import com.example.skillsharing.model.LearningPlan.PlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LearningPlanService {
    
    LearningPlanDTO createPlan(LearningPlanDTO.CreateLearningPlanDTO createPlanDTO);
    
    LearningPlanDTO getPlanById(Long planId);
    
    Page<LearningPlanDTO> getUserPlans(Long userId, Pageable pageable);
    
    List<LearningPlanDTO> getUserPlansByStatus(Long userId, PlanStatus status);
    
    LearningPlanDTO updatePlan(Long planId, LearningPlanDTO.UpdateLearningPlanDTO updatePlanDTO);
    
    void deletePlan(Long planId);
    
    List<LearningPlanDTO> searchPlansByTopic(String topic);
    
    List<LearningPlanDTO> getActiveUserPlans(Long userId);
    
    long countUserPlansByStatus(Long userId, PlanStatus status);
    
    void validatePlanDates(LearningPlanDTO planDTO);
    
    void validatePlanOwnership(Long planId);
    
    LearningPlanDTO convertToDTO(LearningPlan plan);
    
    LearningPlan convertToEntity(LearningPlanDTO planDTO);
    
    // Additional helper methods
    boolean isValidTopic(String topic);
    
    boolean isValidResource(String resource);
    
    void validateTopicsAndResources(List<String> topics, List<String> resources);
}
