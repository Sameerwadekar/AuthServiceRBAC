package com.learn.auth.security.jwt;

import com.learn.auth.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtExpirationMs:900000}")
    private long jwtExpirationMs;

    @Value("${spring.app.jwt.key-id:auth-key-001}")
    private String keyId;

    @Value("${spring.app.jwt.issuer:workflow-auth}")
    private String issuer;

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtUtils(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    // Extract Bearer token from Authorization header in incoming request
    public String getJwtFromHeader(HttpServletRequest request) {
        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7).trim();
        }
        return null;
    }

    // Generate asymmetric RS256 signed JWT
    public String generateTokenFromUsername(UserDetails userDetails) {
        String username = userDetails.getUsername();
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String tenantId = null;
        if (userDetails instanceof User user && user.getTenant() != null) {
            tenantId = user.getTenant().getId();
        }

        var builder = Jwts.builder()
                .header().keyId(keyId).and()
                .issuer(issuer)
                .subject(username)
                .claim("roles", roles);

        if (tenantId != null && !tenantId.isBlank()) {
            builder.claim("tenantId", tenantId);
        }

        return builder
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    // Verify and extract username using public key
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Extract tenantId from JWT claims (null for Super Admin)
    public String getTenantIdFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("tenantId", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Verify token validity using public key
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    // Return Public Key in PEM format for external services/gateways
    public String getPublicKeyPem() {
        String encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" +
                encoded.replaceAll("(.{64})", "$1\n") +
                "\n-----END PUBLIC KEY-----\n";
    }
}
