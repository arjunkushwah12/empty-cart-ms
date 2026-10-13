package com.emptycart.user_service.service;

import com.emptycart.platform.commons.exception.UnauthorizedException;
import com.emptycart.user_service.domain.Role;
import com.emptycart.user_service.domain.User;
import com.emptycart.user_service.dto.LoginRequest;
import com.emptycart.user_service.dto.RegisterUserRequest;
import com.emptycart.user_service.dto.TokenResponse;
import com.emptycart.user_service.dto.UserResponse;
import com.emptycart.user_service.exception.EmailAlreadyExistsException;
import com.emptycart.user_service.repository.UserRepository;
import com.emptycart.user_service.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest req) {
        String email = req.email().trim().toLowerCase();
        if(users.existsByEmail(email)){
            throw new EmailAlreadyExistsException(email);
        }
        User user = new User(req.name().trim(),email,encoder.encode(req.password()), Role.CUSTOMER);
        // Save to database
        user = users.save(user);

        return UserResponse.from(user);
    }

    public TokenResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        User user  = users.findByEmail(email).orElseThrow(() -> new UnauthorizedException("Invalid email or  password"));
        if (!user.isActive() || !encoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateAccessToken(user.getId(),user.getRole());
        return new TokenResponse(token, "Bearer", jwtService.accessTokenSeconds());
    }

}
