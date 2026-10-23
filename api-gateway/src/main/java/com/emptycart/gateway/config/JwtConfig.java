package com.emptycart.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.public-key-path}") String publicPath,
                                 @Value("${jwt.issuer}") String issuer) throws Exception {
        RSAPublicKey pub = (RSAPublicKey) PemKeyLoader.loadPublic(publicPath);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(pub).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}