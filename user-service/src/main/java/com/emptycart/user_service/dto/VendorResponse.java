package com.emptycart.user_service.dto;

import com.emptycart.user_service.domain.Vendor;

public record VendorResponse(Long id, Long userId, String shopName, boolean active) {
    public static VendorResponse from(Vendor v) {
        return new VendorResponse(v.getId(), v.getUser().getId(), v.getShopName(), v.isActive());
    }
}