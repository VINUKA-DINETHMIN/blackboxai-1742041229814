package com.example.skillsharing.base;

import com.example.skillsharing.config.TestConfig;
import com.example.skillsharing.model.User;
import com.example.skillsharing.repository.*;
import com.example.skillsharing.security.TokenProvider;
import com.example.skillsharing.security.UserPrincipal;
import com.example.skillsharing.util.TestDataFactory;
import com.example.skillsharing.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public abstract class BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TokenProvider tokenProvider;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected SkillSharingPostRepository postRepository;

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected LearningPlanRepository learningPlanRepository;

    @Autowired
    protected NotificationRepository notificationRepository;

    protected User testUser;
    protected String authToken;
    protected UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        cleanupDatabase();
        setupTestUser();
        setupAuthentication();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        cleanupDatabase();
    }

    protected void cleanupDatabase() {
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        learningPlanRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected void setupTestUser() {
        testUser = TestDataFactory.createUser(1L);
        testUser = userRepository.save(testUser);
        userPrincipal = UserPrincipal.create(testUser);
    }

    protected void setupAuthentication() {
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        authToken = tokenProvider.createToken(authentication);
    }

    protected String getAuthHeader() {
        return "Bearer " + authToken;
    }

    protected void authenticateUser(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    protected String asJsonString(Object obj) {
        return TestUtil.asJsonString(obj);
    }

    protected User createAndSaveUser(String username, String email) {
        User user = TestDataFactory.createUser(null);
        user.setUsername(username);
        user.setEmail(email);
        return userRepository.save(user);
    }

    protected void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
