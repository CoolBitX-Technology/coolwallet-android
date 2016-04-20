package com.snscity.egdwlib.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by MyPC on 2015/8/24.
 */
public class AES {

    public static final String aes_key_algorithm = "AES";
    public static final String cipher_algorithm = "AES/ECB/NoPadding";


    public static byte[] getAESEncrypt(byte[] src, byte[] salt) {
        try {
            SecretKeySpec key = new SecretKeySpec(salt, aes_key_algorithm);
            Cipher cipher = Cipher.getInstance(cipher_algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(src);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte [] getPBKDF2Encrypt(String algorithm, char[] password, byte[] salt, int iterationCount, int keyLength) {
        try {
//            Security.addProvider(new com.sun.crypto.provider.SunJCE());
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterationCount, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }
}
