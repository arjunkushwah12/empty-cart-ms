package com.emptycart.user_service.service;

import com.emptycart.platform.commons.exception.UnauthorizedException;
import com.emptycart.user_service.domain.RefreshToken;
import com.emptycart.user_service.repository.RefreshTokenRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repo;
    private final long refreshDays;
    public RefreshTokenService(RefreshTokenRepository repo, @Value("${jwt.refresh-token-days:7}") long refreshDays) {
        this.repo = repo;
        this.refreshDays = refreshDays;
    }


    public String newFamily(){
        return UUID.randomUUID().toString();
    }

    @Transactional
    public String issue(Long userId, String familyId){
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repo.save(new RefreshToken(userId,hash(raw),familyId, Instant.now().plus(refreshDays, ChronoUnit.DAYS)));
        return raw;// only the hash is stored
    }
    //norollbackerror: the family revocation must still be saved when we throw

    @Transactional()
    public RefreshToken consume(String raw){
        RefreshToken token = repo.findByTokenHash(hash(raw))
                .orElseThrow(()-> new UnauthorizedException("Invalid refresh token"));
        if(token.isUsed()||token.isRevoked()){
            repo.revokeFamily(token.getFamilyId()); //resue detected: kill the whole fmaily
            throw new UnauthorizedException("Invalid refresh token");
        }
        if(token.getExpiresAt().isBefore(Instant.now())){
            throw new UnauthorizedException("Invalid refresh token");
        }
        token.setUsed(true);
        return repo.save(token);
    }

    @Transactional
    public void revoke(String raw){
        repo.findByTokenHash(hash(raw)).ifPresent(t->repo.revokeFamily(t.getFamilyId()));
    }

    public static String hash(String raw){
        try{
            MessageDigest md = MessageDigest.getInstance(("SHA-256"));
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException e){
            throw new IllegalStateException(e);
        }
    }
}
