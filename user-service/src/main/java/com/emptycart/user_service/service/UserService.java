package com.emptycart.user_service.service;

import com.emptycart.platform.commons.exception.NotFoundException;
import com.emptycart.platform.commons.model.PageResponse;
import com.emptycart.user_service.domain.User;
import com.emptycart.user_service.dto.UpdateProfileRequest;
import com.emptycart.user_service.dto.UserResponse;
import com.emptycart.user_service.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    public UserResponse getById(Long  id){
         return UserResponse.from(find(id));
    }

    public UserResponse updateMe(Long id, UpdateProfileRequest req)
    {
        User u = find(id);
        u.setName(req.name().trim());
        u.setProfilePictureUrl(req.profilePictureUrl());
        return UserResponse.from(users.save(u));
    }

    public PageResponse<UserResponse> list(int page, int size) {
        Page<User> p = users.findAll(PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("id")));
        return new PageResponse<>(p.getContent().stream().map(UserResponse::from).toList(),
                p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    private User find(Long  id){
        return users.findById(id).orElseThrow(()->new NotFoundException("User not found: "+id));
    }
}
