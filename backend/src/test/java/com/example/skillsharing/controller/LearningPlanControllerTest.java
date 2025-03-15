package com.example.skillsharing.controller;

import com.example.skillsharing.base.BaseTest;
import com.example.skillsharing.dto.LearningPlanDTO;
import com.example.skillsharing.model.LearningPlan;
import com.example.skillsharing.model.LearningPlan.PlanStatus;
import com.example.skillsharing.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LearningPlanControllerTest extends BaseTest {

    @Test
    void createPlan_WithValidData_ReturnsCreatedPlan() throws Exception {
        // Arrange
        LearningPlanDTO.CreateLearningPlanDTO createPlanDTO = new LearningPlanDTO.CreateLearningPlanDTO();
        createPlanDTO.setTitle("Test Learning Plan");
        createPlanDTO.setDescription("Test description");
        createPlanDTO.setSkills(Arrays.asList("Java", "Spring Boot"));
        createPlanDTO.setResources(Arrays.asList("Book 1", "Online Course 1"));
        createPlanDTO.setStatus(PlanStatus.NOT_STARTED);

        // Act
        ResultActions result = mockMvc.perform(post("/api/learning-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createPlanDTO))
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(createPlanDTO.getTitle())))
                .andExpect(jsonPath("$.description", is(createPlanDTO.getDescription())))
                .andExpect(jsonPath("$.skills", containsInAnyOrder("Java", "Spring Boot")))
                .andExpect(jsonPath("$.resources", hasSize(2)))
                .andExpect(jsonPath("$.status", is(PlanStatus.NOT_STARTED.toString())));
    }

    @Test
    void getPlan_WithValidId_ReturnsPlan() throws Exception {
        // Arrange
        LearningPlan plan = TestDataFactory.createLearningPlan(null, testUser);
        plan = learningPlanRepository.save(plan);

        // Act
        ResultActions result = mockMvc.perform(get("/api/learning-plans/{planId}", plan.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(plan.getId().intValue())))
                .andExpect(jsonPath("$.title", is(plan.getTitle())))
                .andExpect(jsonPath("$.description", is(plan.getDescription())));
    }

    @Test
    void getUserPlans_ReturnsPaginatedPlans() throws Exception {
        // Arrange
        List<LearningPlan> plans = TestDataFactory.createLearningPlans(3, testUser);
        learningPlanRepository.saveAll(plans);

        // Act
        ResultActions result = mockMvc.perform(get("/api/learning-plans/user/{userId}", testUser.getId())
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void getUserPlansByStatus_ReturnsFilteredPlans() throws Exception {
        // Arrange
        List<LearningPlan> plans = TestDataFactory.createLearningPlans(3, testUser);
        plans.forEach(plan -> plan.setStatus(PlanStatus.IN_PROGRESS));
        learningPlanRepository.saveAll(plans);

        // Act
        ResultActions result = mockMvc.perform(get("/api/learning-plans/user/{userId}/status/{status}",
                testUser.getId(), PlanStatus.IN_PROGRESS)
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].status", everyItem(is(PlanStatus.IN_PROGRESS.toString()))));
    }

    @Test
    void updatePlan_WithValidData_ReturnsUpdatedPlan() throws Exception {
        // Arrange
        LearningPlan plan = TestDataFactory.createLearningPlan(null, testUser);
        plan = learningPlanRepository.save(plan);

        LearningPlanDTO.UpdateLearningPlanDTO updatePlanDTO = new LearningPlanDTO.UpdateLearningPlanDTO();
        updatePlanDTO.setTitle("Updated Title");
        updatePlanDTO.setDescription("Updated description");
        updatePlanDTO.setStatus(PlanStatus.COMPLETED);

        // Act
        ResultActions result = mockMvc.perform(put("/api/learning-plans/{planId}", plan.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updatePlanDTO))
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(updatePlanDTO.getTitle())))
                .andExpect(jsonPath("$.description", is(updatePlanDTO.getDescription())))
                .andExpect(jsonPath("$.status", is(PlanStatus.COMPLETED.toString())));
    }

    @Test
    void deletePlan_WithValidId_DeletesPlan() throws Exception {
        // Arrange
        LearningPlan plan = TestDataFactory.createLearningPlan(null, testUser);
        plan = learningPlanRepository.save(plan);

        // Act
        ResultActions result = mockMvc.perform(delete("/api/learning-plans/{planId}", plan.getId())
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk());
        assertFalse(learningPlanRepository.existsById(plan.getId()));
    }

    @Test
    void searchPlansByTopic_ReturnsMatchingPlans() throws Exception {
        // Arrange
        LearningPlan plan = TestDataFactory.createLearningPlan(null, testUser);
        plan.setTitle("Java Programming");
        plan.setSkills(Arrays.asList("Java", "Spring"));
        learningPlanRepository.save(plan);

        // Act
        ResultActions result = mockMvc.perform(get("/api/learning-plans/search")
                .param("topic", "Java")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].title", containsString("Java")));
    }

    @Test
    void validateTopic_WithValidTopic_ReturnsTrue() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/api/learning-plans/validate-topic")
                .param("topic", "Java Programming")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void validateResource_WithValidResource_ReturnsTrue() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/api/learning-plans/validate-resource")
                .param("resource", "https://www.example.com/course")
                .header("Authorization", getAuthHeader()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
