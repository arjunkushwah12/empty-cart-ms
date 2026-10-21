package com.emptycart.user_service.dto;

import jakarta.validation.constraints.NotNull;

public record VendorStatusRequest(@NotNull Boolean active) {
}
