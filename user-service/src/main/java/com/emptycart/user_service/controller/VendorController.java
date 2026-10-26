package com.emptycart.user_service.controller;

import com.emptycart.platform.commons.model.ApiResponse;
import com.emptycart.platform.commons.model.PageResponse;
import com.emptycart.user_service.dto.VendorApplyRequest;
import com.emptycart.user_service.dto.VendorResponse;
import com.emptycart.user_service.dto.VendorStatusRequest;
import com.emptycart.user_service.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/vendors")
public class VendorController {

    private final VendorService service;

    public VendorController(VendorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VendorResponse>> apply(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody VendorApplyRequest req) {
        VendorResponse created = service.apply(Long.valueOf(jwt.getSubject()), req);
        return ResponseEntity.created(URI.create("/api/v1/vendors"+created.id()))
                .body(ApiResponse.ok(created));
    }

    @GetMapping("/{id}")
    public ApiResponse<VendorResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<VendorResponse> setStatus(@PathVariable Long id, @Valid @RequestBody VendorStatusRequest req) {
        return ApiResponse.ok(service.setStatus(id,req.active()));
    }

    @GetMapping
    public ApiResponse<PageResponse<VendorResponse>> list(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20")int size){
        return ApiResponse.ok(service.list(active,page,size));
    }

    @GetMapping("/me")
    public ApiResponse<VendorResponse> mine(@AuthenticationPrincipal Jwt jwt){
        return ApiResponse.ok(service.getMine(Long.valueOf(jwt.getSubject())));
    }
}
