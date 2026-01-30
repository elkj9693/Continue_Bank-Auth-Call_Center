package com.trustee.vpass.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.MessageDigest;

public class CryptoUtils {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY = "12345678901234567890123456789012"; // 32 bytes for AES-256
    private static final String IV = "1234567890123456"; // 16 bytes

    public enum MaskType {
        NAME, PHONE
    }

    public static String encryptAES256(String text) {
        if (text == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decryptAES256(String encryptedText) {
        if (encryptedText == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public static String generateCI(String name, String phone) {
        try {
            String combined = name + phone + "SALT_CI";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("CI generation failed", e);
        }
    }

    public static String generateDI(String ci, String siteCode) {
        try {
            String combined = ci + siteCode + "SALT_DI";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("DI generation failed", e);
        }
    }

    public static String mask(String text, MaskType type) {
        if (text == null) return null;
        if (type == MaskType.NAME) {
            if (text.length() <= 1) return "*";
            if (text.length() == 2) return text.charAt(0) + "*";
            return text.charAt(0) + "*".repeat(text.length() - 2) + text.charAt(text.length() - 1);
        } else if (type == MaskType.PHONE) {
            String cleaned = text.replaceAll("\\D", "");
            if (cleaned.length() < 7) return text;
            return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 4);
        }
        return text;
    }
}
