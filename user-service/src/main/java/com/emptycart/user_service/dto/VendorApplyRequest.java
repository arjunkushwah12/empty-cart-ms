package com.emptycart.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VendorApplyRequest (@NotBlank @Size(max = 150) String shopName) {}
