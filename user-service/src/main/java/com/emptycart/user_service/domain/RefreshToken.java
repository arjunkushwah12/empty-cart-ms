package com.emptycart.user_service.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="refresh_token")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable =true)
    private Long userId;

    @Column(nullable = false,unique = true,length = 64)
    private String tokenHash;

    @Column(nullable = false, length = 36)
    private String familyId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false,updatable = false)
    private Instant createdAt =  Instant.now();


    public RefreshToken() {
    }

    public RefreshToken(Long userId, String tokenHash, String familyId, Instant expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.familyId = familyId;
        this.expiresAt = expiresAt;
    }

    public Long getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public String getFamilyId() { return familyId; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    public boolean isRevoked() { return revoked; }
}
