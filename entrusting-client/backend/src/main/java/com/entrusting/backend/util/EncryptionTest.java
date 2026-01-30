package com.entrusting.backend.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionTest {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY = "12345678901234567890123456789012";
    private static final String IV = "1234567890123456";

    public static String encrypt(String text) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String text) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(text));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("01012345678 -> " + encrypt("01012345678"));
        System.out.println("010-1234-5678 -> " + encrypt("010-1234-5678"));
        System.out.println("Decrypt Tc0ceu0GxhBDTX7f4r9fkg== -> " + decrypt("Tc0ceu0GxhBDTX7f4r9fkg=="));
        System.out.println("Decrypt zg13oJBqhA68jrgIx43iBw== -> " + decrypt("zg13oJBqhA68jrgIx43iBw=="));
    }
}
