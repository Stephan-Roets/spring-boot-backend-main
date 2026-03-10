package com.todoapp.controller;

import com.todoapp.dto.LoginRequest;
import com.todoapp.dto.LoginResponse;
import com.todoapp.dto.SignupRequest;
import com.todoapp.dto.UserDto;
import com.todoapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequest request) {
        UserDto user = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Account created successfully. Please check your email to verify your account.",
                "user", user
        ));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        String message = authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> request) {
        authService.resendVerification(request.get("email"));
        return ResponseEntity.ok(Map.of(
                "message", "Verification email sent. Please check your inbox."
        ));
    }
}
