package com.learn.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtKeyConfig {

    @Value("${spring.app.jwt.private-key-path:keys/private.pem}")
    private String privateKeyPath;

    @Value("${spring.app.jwt.public-key-path:keys/public.pem}")
    private String publicKeyPath;

    private final ResourceLoader resourceLoader;

    public JwtKeyConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public PrivateKey privateKey() throws Exception {
        String key = readKeyContent(privateKeyPath);
        key = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        String key = readKeyContent(publicKeyPath);
        key = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    private String readKeyContent(String pathStr) throws Exception {
        if (pathStr == null || pathStr.isBlank()) {
            throw new IllegalArgumentException("Key path must not be null or empty");
        }

        // If raw PEM string is provided directly
        if (pathStr.contains("-----BEGIN")) {
            return pathStr;
        }

        // Try direct file path
        Path path = Path.of(pathStr);
        if (Files.exists(path)) {
            return Files.readString(path);
        }

        // Try Spring ResourceLoader (file / classpath)
        String location = pathStr.startsWith("classpath:") || pathStr.startsWith("file:") ? pathStr : "file:" + pathStr;
        Resource resource = resourceLoader.getResource(location);
        if (resource.exists()) {
            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        // Fallback to classpath
        Resource classpathResource = resourceLoader.getResource("classpath:" + pathStr);
        if (classpathResource.exists()) {
            try (InputStream is = classpathResource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        throw new IllegalArgumentException("Key file not found at path: " + pathStr);
    }
}
