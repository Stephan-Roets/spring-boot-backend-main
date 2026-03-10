package com.todoapp.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String phone;
    private String address;
    private String department;
    private String bio;
    private String profilePictureUrl;
    private String role; // Only ADMIN can change roles
}
