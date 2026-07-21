package com.tsundoku.backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsundoku.backend.auth.dto.AuthResponse;
import com.tsundoku.backend.auth.dto.LoginRequest;
import com.tsundoku.backend.auth.dto.RegisterRequest;
import com.tsundoku.backend.auth.mapper.UserMapper;
import com.tsundoku.backend.auth.repository.UserRepository;
import com.tsundoku.backend.auth.service.AuthService;
import com.tsundoku.backend.security.JwtAuthenticationFilter;
import com.tsundoku.backend.security.RateLimiterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private RateLimiterService rateLimiterService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/auth/register debe retornar 201 Created al registrar usuario válido")
    void registerValidUserReturnsCreated() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("user@tsundoku.com", "password123", "Test User");
        AuthResponse authResponse = new AuthResponse("access.token", "refresh.token", 900);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access.token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register debe retornar 400 Bad Request si el formato de email es inválido")
    void registerInvalidEmailReturnsBadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("not-an-email", "password123", "Test User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login debe retornar 200 OK")
    void loginSuccessReturnsOk() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@tsundoku.com", "password123");
        AuthResponse authResponse = new AuthResponse("access.token", "refresh.token", 900);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token"));
    }
}
