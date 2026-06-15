package com.cibertec.edu.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cibertec.edu.models.AutenticacionUsuario;

@Component
public class JwtProvider {
	
	private final SecretKey secret;
    private final long expirationMinutes;
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtProvider.class);

    public JwtProvider(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.secret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String createToken(AutenticacionUsuario authUser){
        Map<String, Object> claims = new HashMap<>();
        claims.put("id",authUser.getId());        
        claims.put("rol", authUser.getRol());
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMinutes * 60 * 1000);
        return Jwts.builder()
                .claims(claims)
                .subject(authUser.getUsername())
                .issuedAt(now)
                .expiration(exp)
                .signWith(secret)
                .compact();        
    }

    public boolean validate(String token){
        try{            
            Jwts.parser().verifyWith(secret).build().parseSignedClaims(token);
        }catch (ExpiredJwtException ex) {
        	LOGGER.warn("Token expirado: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
        	LOGGER.error("Error validando token: {}", ex.getMessage());
            return false;
        }   
        return true;
    }

    public String getUserNameFromToken(String token){
        try{            
            return Jwts.parser().verifyWith(secret)
                    .build()
                    .parseSignedClaims(token).getPayload().getSubject();
        }catch (Exception exception){
            return "Bad token";
        }
    }
}
