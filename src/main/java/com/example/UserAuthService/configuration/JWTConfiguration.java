package com.example.UserAuthService.configuration;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JWTConfiguration {

    @Value("${jwt.secret}")
    String SECRET_KEY_STRING;
//            = "YourSuperSecretKeyForJwtSigningMustBeAtLeast256BitsLong";

    @Bean
    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
    }

}
