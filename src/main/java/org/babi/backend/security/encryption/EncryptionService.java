package org.babi.backend.security.encryption;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

public interface EncryptionService {

    String encrypt(String content);
    String decrypt(String content);

}
