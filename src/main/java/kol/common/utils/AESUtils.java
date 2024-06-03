package kol.common.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.codec.Hex;

import net.bytebuddy.utility.RandomString;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * 安全工具内
 *
 * @author guanzhenggang@gmail.com
 */
public class AESUtils {
    /**
     * 加密
     *
     * @param plainText 原始文本
     * @param password  密码（不能随便输入，使用generateKey生成）
     * @return
     */
    public static String encrypt(String plainText, String passwordHexStr) {

        try {
            byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] passwordBytes = Hex.decode(passwordHexStr);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(passwordBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return new String(Hex.encode(cipher.doFinal(plainBytes)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 解密
     *
     * @param encryptedHexStr
     * @param passwordHexStr
     * @return
     */
    public static String decrypt(String encryptedHexStr, String passwordHexStr) {
        try {
            byte[] encryptedBytes = Hex.decode(encryptedHexStr);
            byte[] passwordBytes = Hex.decode(passwordHexStr);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(passwordBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(encryptedBytes));
        } catch (Exception e) {
            throw new RuntimeException("解密错误");
        }
    }

    /**
     * 生成AES的密码
     *
     * @return
     */
    public static String generateKey(String seed) {
    	if(seed==null) {
    		seed=RandomStringUtils.random(6);
    	}
        KeyGenerator keygen;
        try {
            keygen = KeyGenerator.getInstance("AES");
            //生成128位的随机数，种子随便
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(seed.getBytes());
            keygen.init(128, random);
            byte[] keyBytes = keygen.generateKey().getEncoded();
            return new String(Hex.encode(keyBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public static void main(String[] args) {
        String password = "a154c52565e9e7f94bfc08a1fe702624";
        String encryptedText = "";
        System.out.print("解密后:" + decrypt(encryptedText, password));
        String raw="";
        System.out.print("加密后:" + encrypt(raw, password));
    }

}
