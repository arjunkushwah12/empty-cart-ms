package com.emptycart.user_service.service;

import com.emptycart.platform.commons.exception.ConflictException;
import com.emptycart.platform.commons.exception.ForbiddenException;
import com.emptycart.platform.commons.exception.NotFoundException;
import com.emptycart.platform.commons.model.PageResponse;
import com.emptycart.user_service.domain.Role;
import com.emptycart.user_service.domain.User;
import com.emptycart.user_service.domain.Vendor;
import com.emptycart.user_service.dto.VendorApplyRequest;
import com.emptycart.user_service.dto.VendorResponse;
import com.emptycart.user_service.repository.UserRepository;
import com.emptycart.user_service.repository.VendorRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public PageResponse<VendorResponse> list(Boolean active, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page,0),Math.min(Math.max(size,1),100), Sort.by("id"));
        Page<Vendor> p = active==null?vendors.findAll(pageable):vendors.findByActive(active, pageable);
        return new PageResponse<>(p.getContent().stream().map(VendorResponse::from).toList(),
                p.getNumber(), p.getSize(),p.getTotalElements(),p.getTotalPages());
    }

    public VendorResponse getMine(Long userId){
        return VendorResponse.from(vendors.findByUserId(userId).orElseThrow(()-> new NotFoundException("No vendor application found")));
    }
}
