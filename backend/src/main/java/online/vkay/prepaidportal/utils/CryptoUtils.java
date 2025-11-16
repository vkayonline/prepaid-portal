package online.vkay.prepaidportal.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@UtilityClass
public final class CryptoUtils {

    // ============================================================
    // CONSTANTS (recommended)
    // ============================================================
    public static final String RSA_ALGO = "RSA";
    public static final String RSA_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    public static final String AES_ALGO = "AES";
    public static final String AES_GCM = "AES/GCM/NoPadding";

    public static final int AES_KEY_SIZE = 256;
    public static final int GCM_TAG_BITS = 128;
    public static final int GCM_IV_BYTES = 12;


    // ============================================================
    // RSA: Load Keys
    // ============================================================

    /**
     * Load RSA Private Key from PEM (PKCS8)
     */
    public static PrivateKey loadPrivateKeyFromPem(String pem) throws Exception {
        String base64 = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(base64);
        return KeyFactory.getInstance(RSA_ALGO)
                .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    /**
     * Load RSA Public Key from PEM (X509)
     */
    public static PublicKey loadPublicKeyFromPem(String pem) throws Exception {
        String base64 = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(base64);
        return KeyFactory.getInstance(RSA_ALGO)
                .generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    // ============================================================
    // PEM Encoding Helpers
    // ============================================================

    public static String toPublicPem(Key key) {
        return "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(key.getEncoded()) +
                "\n-----END PUBLIC KEY-----";
    }

    public static String toPrivatePem(Key key) {
        return "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(key.getEncoded()) +
                "\n-----END PRIVATE KEY-----";
    }


    // ============================================================
    // RSA-OAEP Encrypt / Decrypt (Key Wrapping)
    // ============================================================

    public static byte[] rsaEncrypt(PublicKey publicKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_OAEP);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public static byte[] rsaDecrypt(PrivateKey privateKey, byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_OAEP);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }


    // ============================================================
    // AES: Load Keys
    // ============================================================

    public static SecretKey loadAesKey(byte[] raw) {
        return new SecretKeySpec(raw, AES_ALGO);
    }

    public static SecretKey loadAesKey(String base64) {
        return loadAesKey(Base64.getDecoder().decode(base64));
    }


    // ============================================================
    // AES: Generate Key + IV
    // ============================================================

    @SneakyThrows
    public static SecretKey generateAesKey() {
        KeyGenerator gen = KeyGenerator.getInstance(AES_ALGO);
        gen.init(AES_KEY_SIZE);
        return gen.generateKey();
    }

    public static byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_BYTES];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }


    // ============================================================
    // AES-GCM Encrypt / Decrypt
    // ============================================================

    public static EncryptionResult aesGcmEncrypt(SecretKey key, byte[] iv, byte[] plaintext, byte[] aad) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_GCM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        if (aad != null) cipher.updateAAD(aad);

        return new EncryptionResult(cipher.doFinal(plaintext));
    }

    public static byte[] aesGcmDecrypt(SecretKey key, byte[] iv, byte[] ciphertextWithTag, byte[] aad) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_GCM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);

        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        if (aad != null) cipher.updateAAD(aad);

        return cipher.doFinal(ciphertextWithTag);
    }


    // ============================================================
    // Wrapper: encryption result
    // ============================================================

    public record EncryptionResult(byte[] cipherTextWithTag) {
    }
}
