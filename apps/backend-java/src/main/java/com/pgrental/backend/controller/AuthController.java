package com.pgrental.backend.controller;

import com.pgrental.backend.entity.User;
import com.pgrental.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Tag(name = "Auth", description = "OTP-based authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Request OTP for login")
    @PostMapping("/request-otp")
    public Map<String, String> requestOtp(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        User.Role role = User.Role.valueOf(request.get("role").toUpperCase());
        String name = request.get("name");
        
        String otp = authService.requestOtp(phone, role, name);
        return Map.of("message", "OTP sent", "devOtp", otp);
    }

    @Operation(summary = "Verify OTP and get JWT token")
    @PostMapping("/verify-otp")
    public AuthService.AuthResponse verifyOtp(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String otp = request.get("otp");
        User.Role role = User.Role.valueOf(request.get("role").toUpperCase());
        
        return authService.verifyOtp(phone, otp, role);
    }
}
