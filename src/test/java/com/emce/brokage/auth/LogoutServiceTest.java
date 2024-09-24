package com.emce.brokage.auth;

import com.emce.brokage.security.token.Token;
import com.emce.brokage.security.token.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LogoutServiceTest {

    public static final String VALID_TOKEN = "valid-token";
    public static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION = "Authorization";
    @InjectMocks
    private LogoutService logoutService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void testLogout_Success() {
        // Given
        String authHeader = BEARER + VALID_TOKEN;
        Token token = new Token();
        token.setExpired(false);
        token.setRevoked(false);

        when(request.getHeader(AUTHORIZATION))
                .thenReturn(authHeader);
        when(tokenRepository.findByToken(VALID_TOKEN))
                .thenReturn(Optional.of(token));

        // When
        logoutService.logout(request, response, authentication);

        // Then
        assertTrue(token.isExpired());
        assertTrue(token.isRevoked());
        verify(tokenRepository).save(token);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testLogout_NoAuthorizationHeader() {
        // Given
        when(request.getHeader(AUTHORIZATION))
                .thenReturn(null);

        // When
        logoutService.logout(request, response, authentication);

        // Then
        verify(tokenRepository, never()).findByToken(any());
        assertNotNull(SecurityContextHolder.getContext());
    }

    @Test
    void testLogout_InvalidAuthorizationHeader() {
        // Given
        String authHeader = "InvalidHeader";
        when(request.getHeader(AUTHORIZATION))
                .thenReturn(authHeader);

        // When
        logoutService.logout(request, response, authentication);

        // Then
        verify(tokenRepository, never()).findByToken(any());
        assertNotNull(SecurityContextHolder.getContext());
    }

    @Test
    void testLogout_TokenNotFound() {
        // Given
        String tokenStr = VALID_TOKEN;
        String authHeader = BEARER + tokenStr;

        when(request.getHeader(AUTHORIZATION))
                .thenReturn(authHeader);
        when(tokenRepository.findByToken(tokenStr))
                .thenReturn(Optional.empty());

        // When
        logoutService.logout(request, response, authentication);

        // Then
        verify(tokenRepository, never()).save(any());
        assertNotNull(SecurityContextHolder.getContext());
    }
}
