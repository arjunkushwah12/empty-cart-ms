package com.emptycart.user_service.service;

import com.emptycart.platform.commons.exception.UnauthorizedException;
import com.emptycart.user_service.domain.RefreshToken;
import com.emptycart.user_service.domain.Role;
import com.emptycart.user_service.domain.User;
import com.emptycart.user_service.dto.*;
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
    private final JwtService jwt;
    private final RefreshTokenService refreshTokens;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt,RefreshTokenService refreshTokens) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.refreshTokens = refreshTokens;
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

        String token = jwt.generateAccessToken(user.getId(),user.getRole());
        String refresh = refreshTokens.issue(user.getId(),refreshTokens.newFamily());
        return new TokenResponse(token, "Bearer", jwt.accessTokenSeconds(),refresh);
    }

    public TokenResponse refresh(RefreshRequest req) {
        RefreshToken old = refreshTokens.consume(req.refreshToken());
        User user = users.findById(old.getUserId())
                .filter(User::isActive)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        String access = jwt.generateAccessToken(user.getId(), user.getRole());
        String refresh = refreshTokens.issue(user.getId(), old.getFamilyId());
        return new TokenResponse(access, "Bearer", jwt.accessTokenSeconds(), refresh);
    }

    public void logout(RefreshRequest req) {
        refreshTokens.revoke(req.refreshToken());
    }
}
