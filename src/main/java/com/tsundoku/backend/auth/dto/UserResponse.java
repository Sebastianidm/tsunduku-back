package com.tsundoku.backend.auth.dto;

import com.tsundoku.backend.auth.entity.Role;

import java.time.ZonedDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Set<Role> roles,
        ZonedDateTime createdAt
) {}
