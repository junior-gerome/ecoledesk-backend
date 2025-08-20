package com.school.management.security;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

  
    public String generateToken(UserDetails userDetails) {
        return createToken(new HashMap<>(), userDetails);
    }
    
     

    private String createToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        // Vérifier la longueur de la clé
        byte[] keyBytes = secretKey.getBytes();
        if (keyBytes.length < 32) {
            // Étendre la clé avec padding sécurisé
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @PostConstruct
    public void validateSecretKey() {
        if (secretKey.getBytes().length < 32) {
            throw new IllegalStateException("La clé JWT doit faire au moins 32 caractères (256 bits)");
        }
    }

    //  private Key getSigningKey() {
    //     byte[] keyBytes = secretKey.getBytes();
    //     return Keys.hmacShaKeyFor(keyBytes);
    // }

    //   private Key getSigningKey() {
    //     byte[] keyBytes = secretKey.getBytes();
    //     return new SecretkeySpec(KeyBytes,  SignatureAlgorithm.HS256.getJcaName)
    // }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

   
}
