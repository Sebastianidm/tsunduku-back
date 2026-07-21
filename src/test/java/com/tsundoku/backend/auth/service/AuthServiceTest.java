package com.tsundoku.backend.auth.service;

import com.tsundoku.backend.auth.dto.AuthResponse;
import com.tsundoku.backend.auth.dto.LoginRequest;
import com.tsundoku.backend.auth.dto.RefreshTokenRequest;
import com.tsundoku.backend.auth.dto.RegisterRequest;
import com.tsundoku.backend.auth.entity.Role;
import com.tsundoku.backend.auth.entity.User;
import com.tsundoku.backend.auth.repository.UserRepository;
import com.tsundoku.backend.common.exception.BadRequestException;
import com.tsundoku.backend.common.exception.UnauthorizedException;
import com.tsundoku.backend.security.JwtTokenProvider;
import com.tsundoku.backend.security.TokenRevocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private TokenRevocationService tokenRevocationService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("user@tsundoku.com")
                .password("encodedPassword")
                .fullName("Test User")
                .roles(Set.of(Role.ROLE_USER))
                .build();

        registerRequest = new RegisterRequest("user@tsundoku.com", "password123", "Test User");
    }

    @Test
    @DisplayName("Registro exitoso debe guardar usuario y generar tokens")
    void registerSuccess() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateAccessToken(any(), any(), any())).thenReturn("access.token");
        when(tokenProvider.generateRefreshToken(any())).thenReturn("refresh.token");
        when(tokenProvider.getJwtExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("access.token", response.accessToken());
        assertEquals("refresh.token", response.refreshToken());
        verify(userRepository, times(1)).save(any(User.class));
        verify(tokenRevocationService, times(1)).saveRefreshToken(any(), eq("refresh.token"), anyLong());
    }

    @Test
    @DisplayName("Registro fallido si el email ya existe")
    void registerEmailExistsThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login exitoso retorna tokens JWT")
    void loginSuccess() {
        LoginRequest loginRequest = new LoginRequest("user@tsundoku.com", "password123");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateAccessToken(authentication)).thenReturn("access.token");
        when(tokenProvider.getUserIdFromToken("access.token")).thenReturn(1L);
        when(tokenProvider.generateRefreshToken(1L)).thenReturn("refresh.token");
        when(tokenProvider.getJwtExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access.token", response.accessToken());
        assertEquals("refresh.token", response.refreshToken());
    }

    @Test
    @DisplayName("Refresh token exitoso rota el token")
    void refreshTokenSuccess() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid.refresh.token");

        when(tokenProvider.validateToken("valid.refresh.token")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("valid.refresh.token")).thenReturn(1L);
        when(tokenRevocationService.isValidRefreshToken(1L, "valid.refresh.token")).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(tokenProvider.generateAccessToken(anyLong(), anyString(), anyList())).thenReturn("new.access.token");
        when(tokenProvider.generateRefreshToken(1L)).thenReturn("new.refresh.token");
        when(tokenProvider.getJwtExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new.access.token", response.accessToken());
        assertEquals("new.refresh.token", response.refreshToken());
        verify(tokenRevocationService, times(1)).saveRefreshToken(eq(1L), eq("new.refresh.token"), anyLong());
    }

    @Test
    @DisplayName("Refresh token fallido si el token fue revocado")
    void refreshTokenRevokedThrowsException() {
        RefreshTokenRequest request = new RefreshTokenRequest("revoked.refresh.token");

        when(tokenProvider.validateToken("revoked.refresh.token")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("revoked.refresh.token")).thenReturn(1L);
        when(tokenRevocationService.isValidRefreshToken(1L, "revoked.refresh.token")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(request));
    }
}
