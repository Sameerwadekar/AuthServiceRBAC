package com.learn.auth.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

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

    @Value("${spring.app.refreshExpirationMs:604800000}")
    private long refreshExpirationMs;

    @Value("${spring.app.jwtCookieName:accessToken}")
    private String jwtCookie;

    @Value("${spring.app.jwtRefreshCookieName:refreshToken}")
    private String jwtRefreshCookie;

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

    // Generate Access Token ResponseCookie from UserDetails
    public ResponseCookie generateAccessTokenCookie(UserDetails userDetails) {
        String jwt = generateTokenFromUsername(userDetails);
        return generateAccessTokenCookie(jwt);
    }

    // Generate Access Token ResponseCookie from token string
    public ResponseCookie generateAccessTokenCookie(String token) {
        return ResponseCookie.from(jwtCookie, token)
                .path("/")
                .maxAge(jwtExpirationMs / 1000)
                .httpOnly(true)
                .secure(false) // Set to true in Production with HTTPS
                .sameSite("Lax")
                .build();
    }

    // Generate Refresh Token ResponseCookie
    public ResponseCookie generateRefreshTokenCookie(String refreshTokenStr) {
        return ResponseCookie.from(jwtRefreshCookie, refreshTokenStr)
                .path("/")
                .maxAge(refreshExpirationMs / 1000)
                .httpOnly(true)
                .secure(false) // Set to true in Production with HTTPS
                .sameSite("Lax")
                .build();
    }

    // Clear Access Token Cookie (for Logout)
    public ResponseCookie getCleanAccessTokenCookie() {
        return ResponseCookie.from(jwtCookie, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .sameSite("Lax")
                .build();
    }

    // Clear Refresh Token Cookie (for Logout)
    public ResponseCookie getCleanRefreshTokenCookie() {
        return ResponseCookie.from(jwtRefreshCookie, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .sameSite("Lax")
                .build();
    }

    // Extract Access Token from Cookie in Incoming Request
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    // Extract Refresh Token from Cookie in Incoming Request
    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtRefreshCookie);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    // Generate asymmetric RS256 signed JWT
    public String generateTokenFromUsername(UserDetails userDetails) {
        String username = userDetails.getUsername();
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .header().keyId(keyId).and()
                .issuer(issuer)
                .subject(username)
                .claim("roles", roles)
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
