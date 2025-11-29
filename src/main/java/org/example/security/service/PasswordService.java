package org.example.security.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final BCrypt.Hasher bcryptHasher = BCrypt.withDefaults();
    private final BCrypt.Verifyer bcryptVerifyer = BCrypt.verifyer();

    public String hashPassword(String plainPassword) {
        String hash = bcryptHasher.hashToString(12, plainPassword.toCharArray());
        System.out.println("Password hashed successfully");
        return hash;
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        System.out.println("Verifying password...");
        BCrypt.Result result = bcryptVerifyer.verify(plainPassword.toCharArray(), hashedPassword);
        System.out.println("Verification result: " + result.verified);
        return result.verified;
    }

    public String hashUsername(String username) {
        // Используем SHA-256 для имени вместо BCrypt
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(username.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String result = hexString.toString();
            System.out.println("Username hash: " + username + " -> " + result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error hashing username", e);
        }
    }
}