package com.supplysync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplysync.dto.request.LoginRequest;
import com.supplysync.dto.request.RegisterRequest;
import com.supplysync.dto.request.TokenRefreshRequest;
import com.supplysync.entity.User;
import com.supplysync.enums.UserRole;
import com.supplysync.repository.UserRepository;
import com.supplysync.security.JwtService;
import com.supplysync.security.LoginRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        when(rateLimiter.isAllowed(anyString())).thenReturn(true);
    }

    @Test
    void register_shouldReturn201_withValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setFullName("New User");
        request.setRole(UserRole.STAFF);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void register_shouldReturn409_whenUsernameAlreadyExists() throws Exception {
        User existingUser = User.builder()
                .username("existing")
                .email("existing@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .fullName("Existing User")
                .role(UserRole.STAFF)
                .isActive(true)
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setEmail("other@test.com");
        request.setPassword("password123");
        request.setFullName("Other User");
        request.setRole(UserRole.STAFF);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_shouldReturn200_withValidCredentials() throws Exception {
        User existingUser = User.builder()
                .username("loginuser")
                .email("login@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Login User")
                .role(UserRole.STAFF)
                .isActive(true)
                .build();
        userRepository.save(existingUser);

        LoginRequest request = new LoginRequest();
        request.setUsername("loginuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void login_shouldReturn401_withInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("invaliduser");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_shouldReturn200_withValidRefreshToken() throws Exception {
        User user = User.builder()
                .username("refreshuser")
                .email("refresh@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Refresh User")
                .role(UserRole.STAFF)
                .isActive(true)
                .build();
        userRepository.save(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void login_shouldReturn429_afterExceedingRateLimitAttempts() throws Exception {
        // Configure the mocked StringRedisTemplate to simulate 5 prior failures
        // so isAllowed() returns false for the test IP
        when(rateLimiter.isAllowed(anyString())).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setUsername("anyuser");
        request.setPassword("anypassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.errorCode").value("TOO_MANY_LOGIN_ATTEMPTS"));
    }
}
