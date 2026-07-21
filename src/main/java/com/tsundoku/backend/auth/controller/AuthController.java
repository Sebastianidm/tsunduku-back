package com.tsundoku.backend.auth.controller;

import com.tsundoku.backend.auth.dto.*;
import com.tsundoku.backend.auth.entity.User;
import com.tsundoku.backend.auth.mapper.UserMapper;
import com.tsundoku.backend.auth.repository.UserRepository;
import com.tsundoku.backend.auth.service.AuthService;
import com.tsundoku.backend.common.exception.ResourceNotFoundException;
import com.tsundoku.backend.security.RateLimiterService;
import com.tsundoku.backend.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para registro, inicio de sesión y gestión de tokens")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea una nueva cuenta de usuario y retorna tokens de acceso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o email ya registrado"),
            @ApiResponse(responseCode = "429", description = "Demasiadas peticiones (Rate Limit Exceeded)")
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest) {
        
        rateLimiterService.checkAuthRateLimit(getClientIp(httpServletRequest));
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario con email y contraseña y retorna tokens JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "429", description = "Demasiadas peticiones (Rate Limit Exceeded)")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {

        rateLimiterService.checkAuthRateLimit(getClientIp(httpServletRequest));
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token de acceso", description = "Genera un nuevo Access Token y rota el Refresh Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Refresh Token inválido o revocado")
    })
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpServletRequest) {

        rateLimiterService.checkAuthRateLimit(getClientIp(httpServletRequest));
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cerrar sesión", description = "Invalida los tokens del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión cerrada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        authService.logout(userPrincipal.getId(), authorizationHeader);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener perfil del usuario autenticado", description = "Retorna los datos del usuario actualmente logueado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil retornado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
