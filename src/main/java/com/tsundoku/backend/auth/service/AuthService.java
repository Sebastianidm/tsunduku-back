package com.tsundoku.backend.auth.service;

import com.tsundoku.backend.auth.dto.*;
import com.tsundoku.backend.auth.entity.Role;
import com.tsundoku.backend.auth.entity.User;
import com.tsundoku.backend.auth.repository.UserRepository;
import com.tsundoku.backend.common.exception.BadRequestException;
import com.tsundoku.backend.common.exception.UnauthorizedException;
import com.tsundoku.backend.security.JwtTokenProvider;
import com.tsundoku.backend.security.TokenRevocationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final TokenRevocationService tokenRevocationService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("El email ya se encuentra registrado");
        }

        User user = User.builder()
                .email(request.email().toLowerCase().trim())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .roles(Set.of(Role.ROLE_USER))
                .build();

        userRepository.save(user);
        log.info("Usuario registrado exitosamente con ID: {}", user.getId());

        String accessToken = tokenProvider.generateAccessToken(
                user.getId(), 
                user.getEmail(), 
                user.getRoles().stream().map(Enum::name).toList()
        );
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        tokenRevocationService.saveRefreshToken(user.getId(), refreshToken, tokenProvider.getRefreshTokenExpirationMs());

        return new AuthResponse(accessToken, refreshToken, tokenProvider.getJwtExpirationMs() / 1000);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email().toLowerCase().trim(),
                        request.password()
                )
        );

        String accessToken = tokenProvider.generateAccessToken(authentication);
        Long userId = tokenProvider.getUserIdFromToken(accessToken);
        String refreshToken = tokenProvider.generateRefreshToken(userId);

        tokenRevocationService.saveRefreshToken(userId, refreshToken, tokenProvider.getRefreshTokenExpirationMs());

        log.info("Inicio de sesión exitoso para usuario ID: {}", userId);
        return new AuthResponse(accessToken, refreshToken, tokenProvider.getJwtExpirationMs() / 1000);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("El Refresh Token es inválido o ha expirado");
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);

        if (!tokenRevocationService.isValidRefreshToken(userId, refreshToken)) {
            throw new UnauthorizedException("El Refresh Token ha sido revocado o no coincide");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        // OWASP A07: Rotación de Refresh Token
        String newAccessToken = tokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream().map(Enum::name).toList()
        );
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId());

        tokenRevocationService.saveRefreshToken(user.getId(), newRefreshToken, tokenProvider.getRefreshTokenExpirationMs());

        log.info("Refresh token rotado exitosamente para usuario ID: {}", userId);
        return new AuthResponse(newAccessToken, newRefreshToken, tokenProvider.getJwtExpirationMs() / 1000);
    }

    public void logout(Long userId, String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            String token = accessToken.substring(7);
            long remainingTime = tokenProvider.getExpirationMsFromToken(token);
            tokenRevocationService.blacklistToken(token, remainingTime);
        }
        tokenRevocationService.revokeRefreshToken(userId);
        log.info("Cierre de sesión y revocación de tokens para usuario ID: {}", userId);
    }
}
