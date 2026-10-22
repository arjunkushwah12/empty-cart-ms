package com.emptycart.user_service.security;


import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Bean
    public JwtEncoder jwtEncoder(@Value("${jwt.private-key-path}") String privatePath,
                                 @Value("${jwt.public-key-path}") String publicPath,
                                 @Value("${jwt.key-id:emptycart-key-1}") String keyId) throws Exception {
        RSAPrivateKey priv = (RSAPrivateKey) PemKeyLoader.loadPrivate(privatePath);
        RSAPublicKey pub = (RSAPublicKey) PemKeyLoader.loadPublic(publicPath);
        RSAKey jwk = new RSAKey.Builder(pub).privateKey(priv).keyID(keyId).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.public-key-path}") String publicPath,
                                 @Value("${jwt.issuer}") String issuer) throws Exception {
        RSAPublicKey pub = (RSAPublicKey) PemKeyLoader.loadPublic(publicPath);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(pub).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}