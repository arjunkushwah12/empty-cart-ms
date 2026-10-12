package com.emptycart.user_service;

import com.emptycart.user_service.domain.Role;
import com.emptycart.user_service.security.JwtService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void tokenIsSignedWithPrivateKeyAndVerifiedWithPublicKey() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair kp = gen.generateKeyPair();
        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();

        RSAKey jwk = new RSAKey.Builder(pub).privateKey((RSAPrivateKey) kp.getPrivate()).keyID("k1").build();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
        JwtService service = new JwtService(encoder, "http://localhost:8081", 15, "k1");

        String token = service.generateAccessToken(42L, Role.CUSTOMER);

        JwtDecoder decoder = NimbusJwtDecoder.withPublicKey(pub).build();
        Jwt jwt = decoder.decode(token);
        assertEquals("42", jwt.getSubject());
        assertEquals("http://localhost:8081", jwt.getIssuer().toString());
        assertEquals(List.of("CUSTOMER"), jwt.getClaimAsStringList("roles"));
        assertEquals("RS256", jwt.getHeaders().get("alg"));

        String tampered = token.substring(0, token.length() - 4) + "AAAA";
        assertThrows(JwtException.class, () -> decoder.decode(tampered));
    }
}