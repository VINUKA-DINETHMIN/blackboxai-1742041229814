package com.example.skillsharing.service.impl;

import com.example.skillsharing.dto.LearningPlanDTO;
import com.example.skillsharing.exception.BadRequestException;
import com.example.skillsharing.exception.ResourceNotFoundException;
import com.example.skillsharing.model.LearningPlan;
import com.example.skillsharing.model.LearningPlan.PlanStatus;
import com.example.skillsharing.model.User;
import com.example.skillsharing.repository.LearningPlanRepository;
import com.example.skillsharing.service.LearningPlanService;
import com.example.skillsharing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningPlanServiceImpl implements LearningPlanService {

    private final LearningPlanRepository learningPlanRepository;
    private final UserService userService;

    private static final int MAX_TOPICS = 10;
    private static final int MAX_RESOURCES = 20;
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$"
    );

    @Override
    public LearningPlanDTO createPlan(LearningPlanDTO.CreateLearningPlanDTO createPlanDTO) {
        User currentUser = userService.getCurrentUser();

        validateTopicsAndResources(createPlanDTO.getTopics(), createPlanDTO.getResources());

        LearningPlan plan = new LearningPlan();
        plan.setUser(currentUser);
        plan.setTitle(createPlanDTO.getTitle());
        plan.setDescription(createPlanDTO.getDescription());
        plan.setTopics(createPlanDTO.getTopics());
        plan.setResources(createPlanDTO.getResources());
        plan.setStartDate(createPlanDTO.getStartDate());
        plan.setEndDate(createPlanDTO.getEndDate());
        plan.setStatus(PlanStatus.NOT_STARTED);

        validatePlanDates(convertToDTO(plan));

        LearningPlan savedPlan = learningPlanRepository.save(plan);
        return convertToDTO(savedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPlanDTO getPlanById(Long planId) {
        LearningPlan plan = getPlanEntityById(planId);
        return convertToDTO(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearningPlanDTO> getUserPlans(Long userId, Pageable pageable) {
        User user = userService.getUserEntityById(userId);
        return learningPlanRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPlanDTO> getUserPlansByStatus(Long userId, PlanStatus status) {
        User user = userService.getUserEntityById(userId);
        return learningPlanRepository.findByUserAndStatusOrderByStartDateAsc(user, status)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public LearningPlanDTO updatePlan(Long planId, LearningPlanDTO.UpdateLearningPlanDTO updatePlanDTO) {
        LearningPlan plan = getPlanEntityById(planId);
        validatePlanOwnership(planId);
        validateTopicsAndResources(updatePlanDTO.getTopics(), updatePlanDTO.getResources());

        plan.setTitle(updatePlanDTO.getTitle());
        plan.setDescription(updatePlanDTO.getDescription());
        plan.setTopics(updatePlanDTO.getTopics());
        plan.setResources(updatePlanDTO.getResources());
        plan.setStartDate(updatePlanDTO.getStartDate());
        plan.setEndDate(updatePlanDTO.getEndDate());
        plan.setStatus(updatePlanDTO.getStatus());

        validatePlanDates(convertToDTO(plan));

        LearningPlan updatedPlan = learningPlanRepository.save(plan);
        return convertToDTO(updatedPlan);
    }

    @Override
    public void deletePlan(Long planId) {
        validatePlanOwnership(planId);
        learningPlanRepository.deleteById(planId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPlanDTO> searchPlansByTopic(String topic) {
        return learningPlanRepository.findByTopicsContainingIgnoreCase(topic)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPlanDTO> getActiveUserPlans(Long userId) {
        User user = userService.getUserEntityById(userId);
        return learningPlanRepository.findActiveUserPlans(user, LocalDateTime.now())
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUserPlansByStatus(Long userId, PlanStatus status) {
        User user = userService.getUserEntityById(userId);
        return learningPlanRepository.countByUserAndStatus(user, status);
    }

    @Override
    public void validatePlanDates(LearningPlanDTO planDTO) {
        if (planDTO.getStartDate().isAfter(planDTO.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }
        if (planDTO.getStartDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start date cannot be in the past");
        }
    }

    @Override
    public void validatePlanOwnership(Long planId) {
        User currentUser = userService.getCurrentUser();
        LearningPlan plan = getPlanEntityById(planId);
        
        if (!plan.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't have permission to modify this learning plan");
        }
    }

    @Override
    public boolean isValidTopic(String topic) {
        return topic != null && 
               !topic.trim().isEmpty() && 
               topic.length() <= 50;
    }

    @Override
    public boolean isValidResource(String resource) {
        return resource != null && 
               !resource.trim().isEmpty() && 
               (resource.length() <= 255) &&
               (URL_PATTERN.matcher(resource).matches() || 
                resource.startsWith("book:") || 
                resource.startsWith("course:"));
    }

    @Override
    public void validateTopicsAndResources(List<String> topics, List<String> resources) {
        if (topics.size() > MAX_TOPICS) {
            throw new BadRequestException("Maximum " + MAX_TOPICS + " topics allowed");
        }
        if (resources.size() > MAX_RESOURCES) {
            throw new BadRequestException("Maximum " + MAX_RESOURCES + " resources allowed");
        }

        if (topics.stream().anyMatch(topic -> !isValidTopic(topic))) {
            throw new BadRequestException("Invalid topic found. Topics must be between 1 and 50 characters");
        }
        if (resources.stream().anyMatch(resource -> !isValidResource(resource))) {
            throw new BadRequestException("Invalid resource URL or format found");
        }
    }

    @Override
    public LearningPlanDTO convertToDTO(LearningPlan plan) {
        LearningPlanDTO dto = new LearningPlanDTO();
        dto.setId(plan.getId());
        dto.setTitle(plan.getTitle());
        dto.setDescription(plan.getDescription());
        dto.setTopics(plan.getTopics());
        dto.setResources(plan.getResources());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());
        dto.setStatus(plan.getStatus());
        dto.setUser(userService.convertToDTO(plan.getUser()));
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());
        return dto;
    }

    @Override
    public LearningPlan convertToEntity(LearningPlanDTO planDTO) {
        LearningPlan plan = new LearningPlan();
        plan.setTitle(planDTO.getTitle());
        plan.setDescription(planDTO.getDescription());
        plan.setTopics(planDTO.getTopics());
        plan.setResources(planDTO.getResources());
        plan.setStartDate(planDTO.getStartDate());
        plan.setEndDate(planDTO.getEndDate());
        plan.setStatus(planDTO.getStatus());
        return plan;
    }

    private LearningPlan getPlanEntityById(Long planId) {
        return learningPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning Plan", "id", planId));
    }
}
