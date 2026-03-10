package com.todoapp.controller;

import com.todoapp.dto.UpdateUserRequest;
import com.todoapp.dto.UserDto;
import com.todoapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(userService.getCurrentUser(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(
            Authentication authentication,
            @RequestBody UpdateUserRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(userService.updateUser(userId, request, userId));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(userService.updateUser(id, request, requestingUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        userService.deleteUser(id, requestingUserId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
