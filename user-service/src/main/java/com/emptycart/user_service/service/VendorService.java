package com.emptycart.user_service.service;

import com.emptycart.platform.commons.exception.ConflictException;
import com.emptycart.platform.commons.exception.ForbiddenException;
import com.emptycart.platform.commons.exception.NotFoundException;
import com.emptycart.user_service.domain.Role;
import com.emptycart.user_service.domain.User;
import com.emptycart.user_service.domain.Vendor;
import com.emptycart.user_service.dto.VendorApplyRequest;
import com.emptycart.user_service.dto.VendorResponse;
import com.emptycart.user_service.repository.UserRepository;
import com.emptycart.user_service.repository.VendorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class VendorService {
    private final VendorRepository vendors;
    private final UserRepository users;

    public VendorService(VendorRepository vendors, UserRepository users) {
        this.vendors = vendors;
        this.users = users;
    }

    @Transactional
    public VendorResponse apply(Long userId, VendorApplyRequest req)
    {
        User user = users.findById(userId).orElseThrow(()-> new NotFoundException("User not found: "+userId));
        if(user.getRole() != Role.CUSTOMER)
        {
            throw new ForbiddenException("Only Customers can apply to become a Vendor");
        }
        if(vendors.findByUserId(userId).isPresent()){
            throw new ConflictException("Vendor application already exists");
        }
        return VendorResponse.from(vendors.save(new Vendor(user, req.shopName().trim())));
    }

    public VendorResponse get(Long id) {
        return VendorResponse.from(find(id));
    }

    @Transactional
    public VendorResponse setStatus(Long id, boolean active) {
        Vendor v = find(id);
        v.setActive(active);
        User u = v.getUser();
        if (active) {
            u.setRole(Role.VENDOR);
        }
        else if(u.getRole() == Role.VENDOR){
            u.setRole(Role.CUSTOMER);
        }
        users.save(u);
        return VendorResponse.from(vendors.save(v));
    }
    private Vendor find(Long id) {
        return vendors.findById(id).orElseThrow(()-> new NotFoundException("Vendor not found: "+id));
    }
}
