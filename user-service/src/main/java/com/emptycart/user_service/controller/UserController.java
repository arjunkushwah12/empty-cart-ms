package com.emptycart.user_service.controller;


import com.emptycart.platform.commons.model.ApiResponse;
import com.emptycart.platform.commons.model.PageResponse;
import com.emptycart.user_service.dto.UpdateProfileRequest;
import com.emptycart.user_service.dto.UserResponse;
import com.emptycart.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;
    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(service.getById(Long.valueOf(jwt.getSubject())));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@AuthenticationPrincipal Jwt jwt,
                                              @Valid @RequestBody UpdateProfileRequest req) {
        return
                ApiResponse.ok(
                        service.updateMe(Long.valueOf(jwt.getSubject()), req)
                );
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> list(@RequestParam(defaultValue= "0")int page,@RequestParam(defaultValue ="20") int size)
    {
        return ApiResponse.ok(service.list(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable Long id)
    {
        return ApiResponse.ok(service.getById(id));
    }
}
