package com.emptycart.user_service.security;

import com.emptycart.user_service.domain.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final String issuer;
    private final long accessMinutes;
    private final String keyId;

    public JwtService(JwtEncoder encoder,
                      @Value("${jwt.issuer}") String issuer,
                      @Value("${jwt.access-token-minutes}") long accessMinutes,
                      @Value("${jwt.key-id:emptycart-key-1}") String keyId) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.accessMinutes = accessMinutes;
        this.keyId = keyId;
    }
    public String generateAccessToken(Long userId, Role role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plus(accessMinutes, ChronoUnit.MINUTES))
                .claim("roles", List.of(role.name()))
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(keyId).build();
        return  encoder.encode(JwtEncoderParameters.from(header,claims)).getTokenValue();
    }

    public long accessTokenSeconds() {
        return accessMinutes * 60;
    }
}
