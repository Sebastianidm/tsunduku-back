package com.tsundoku.backend.auth.mapper;

import com.tsundoku.backend.auth.dto.UserResponse;
import com.tsundoku.backend.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles(),
                user.getCreatedAt()
        );
    }
}
