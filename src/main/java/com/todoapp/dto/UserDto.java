package com.todoapp.dto;

import com.todoapp.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private String name;
    private String phone;
    private String address;
    private String department;
    private String role;
    private String profilePictureUrl;
    private String bio;
    private Boolean emailVerified;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .department(user.getDepartment())
                .role(user.getRole().name())
                .profilePictureUrl(user.getProfilePictureUrl())
                .bio(user.getBio())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
