package org.babi.backend.security.encryption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.babi.backend.security.encryption.exception.EncryptionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Service
public class EncryptionServiceImpl implements EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String secretKey;
    private SecretKeySpec secretKeySpec;

    @PostConstruct
    public void init() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digested = messageDigest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            digested = Arrays.copyOf(digested, 16);
            secretKeySpec = new SecretKeySpec(digested, "AES");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String encrypt(String content) {
        try {
            Cipher cipher = cipher();
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decrypt(String content) {
        Cipher cipher = cipher();
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        } catch (InvalidKeyException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        try {
            return new String(cipher.doFinal(Base64.getDecoder().decode(content)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Cipher cipher() {
        try {
            return Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Value("${babi.security.encryption.secretKey}")
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
