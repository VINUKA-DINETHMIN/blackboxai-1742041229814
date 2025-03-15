package com.example.skillsharing.controller;

import com.example.skillsharing.dto.LearningPlanDTO;
import com.example.skillsharing.model.LearningPlan.PlanStatus;
import com.example.skillsharing.security.CurrentUser;
import com.example.skillsharing.security.UserPrincipal;
import com.example.skillsharing.service.LearningPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-plans")
@RequiredArgsConstructor
public class LearningPlanController {

    private final LearningPlanService learningPlanService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public LearningPlanDTO createPlan(@Valid @RequestBody LearningPlanDTO.CreateLearningPlanDTO createPlanDTO) {
        return learningPlanService.createPlan(createPlanDTO);
    }

    @GetMapping("/{planId}")
    public LearningPlanDTO getPlan(@PathVariable Long planId) {
        return learningPlanService.getPlanById(planId);
    }

    @GetMapping("/user/{userId}")
    public Page<LearningPlanDTO> getUserPlans(@PathVariable Long userId, Pageable pageable) {
        return learningPlanService.getUserPlans(userId, pageable);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public List<LearningPlanDTO> getUserPlansByStatus(@PathVariable Long userId,
                                                     @PathVariable PlanStatus status) {
        return learningPlanService.getUserPlansByStatus(userId, status);
    }

    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('USER')")
    public LearningPlanDTO updatePlan(@PathVariable Long planId,
                                     @Valid @RequestBody LearningPlanDTO.UpdateLearningPlanDTO updatePlanDTO) {
        return learningPlanService.updatePlan(planId, updatePlanDTO);
    }

    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deletePlan(@PathVariable Long planId) {
        learningPlanService.deletePlan(planId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public List<LearningPlanDTO> searchPlansByTopic(@RequestParam String topic) {
        return learningPlanService.searchPlansByTopic(topic);
    }

    @GetMapping("/user/{userId}/active")
    public List<LearningPlanDTO> getActiveUserPlans(@PathVariable Long userId) {
        return learningPlanService.getActiveUserPlans(userId);
    }

    @GetMapping("/user/{userId}/count/{status}")
    public ResponseEntity<Long> countUserPlansByStatus(@PathVariable Long userId,
                                                      @PathVariable PlanStatus status) {
        long count = learningPlanService.countUserPlansByStatus(userId, status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/validate-topic")
    public ResponseEntity<Boolean> isValidTopic(@RequestParam String topic) {
        boolean isValid = learningPlanService.isValidTopic(topic);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/validate-resource")
    public ResponseEntity<Boolean> isValidResource(@RequestParam String resource) {
        boolean isValid = learningPlanService.isValidResource(resource);
        return ResponseEntity.ok(isValid);
    }
}
