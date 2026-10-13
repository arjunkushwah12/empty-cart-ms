package com.emptycart.user_service.controller;

import com.emptycart.platform.commons.model.ApiResponse;
import com.emptycart.user_service.dto.LoginRequest;
import com.emptycart.user_service.dto.RegisterUserRequest;
import com.emptycart.user_service.dto.TokenResponse;
import com.emptycart.user_service.dto.UserResponse;
import com.emptycart.user_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterUserRequest req){
        UserResponse created = service.register(req);
        return ResponseEntity.created(URI.create("/api/v1/users"+ created.id()))
                .body(ApiResponse.ok(created));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req){
        return ApiResponse.ok(service.login(req));
    }

}
