package com.emptycart.user_service.repository;

import com.emptycart.user_service.domain.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUserId(Long userId);
    Page<Vendor> findByActive(boolean active, Pageable pageable);
}