package com.emce.brokage.auth;

import com.emce.brokage.auth.dto.AuthRequest;
import com.emce.brokage.auth.dto.AuthResponse;
import com.emce.brokage.auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();  // To serialize and deserialize JSON

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void registerUser_shouldReturnCreatedStatus_whenValidRequest() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest("testName", "test@example.com", "Password1234!.@");
        String jwtToken = "some-jwt-token";
        AuthResponse authResponse = new AuthResponse(jwtToken, new Date(System.currentTimeMillis()+60000L));

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(jwtToken));
    }
    @Test
    void registerUser_shouldReturnValidationError_whenEmailNotValid() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest("testName", "invalidmail", "Password1234!.@");
        String jwtToken = "some-jwt-token";
        AuthResponse authResponse = new AuthResponse(jwtToken, new Date(System.currentTimeMillis()+60000L));

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_shouldReturnValidationError_whenPasswordNotValid() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest("testName", "test@example.com", "Password123");
        String jwtToken = "some-jwt-token";
        AuthResponse authResponse = new AuthResponse(jwtToken, new Date(System.currentTimeMillis()+60000L));

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnOkStatus_whenValidRequest() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("test@example.com", "password123");
        String jwtToken = "some-jwt-token";
        AuthResponse authResponse = new AuthResponse(jwtToken, new Date(System.currentTimeMillis()+60000L));

        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwtToken));
    }
    @Test
    void login_shouldReturnOkStatus_whenEmptyMail() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("", "password123");
        String jwtToken = "some-jwt-token";
        AuthResponse authResponse = new AuthResponse(jwtToken, new Date(System.currentTimeMillis()+60000L));

        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void login_shouldReturnOkStatus_whenEmptyPassword() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("test@example.com", "");
        String jwtToken = "some-jwt-token";
        AuthResponse authResponse = new AuthResponse(jwtToken, new Date(System.currentTimeMillis()+60000L));

        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateToken_shouldReturnUserId_whenTokenIsValid() throws Exception {
        // Given
        String token = "valid-token";
        Integer userId = 1;

        when(authService.validateToken(eq(token))).thenReturn(userId);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validate")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));  // Verifies that the response body is "1"
    }
}
