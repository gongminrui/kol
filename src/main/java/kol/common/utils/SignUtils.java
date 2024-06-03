package kol.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;

import lombok.extern.slf4j.Slf4j;

/**
 * @author kent
 */
@Slf4j
public class SignUtils {

    /**
     * 加签
     *
     * @param data
     * @param secretKey
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String sign(String data, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return Base64.encodeBase64String(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
        } catch (Exception ex) {
            log.error("sign方法：", ex);
            throw new RuntimeException("加签错误");
        }
    }

    /**
     * 验签
     *
     * @param data
     * @param secretKey
     * @param sign
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static boolean verify(String data, String secretKey, String sign) {
        String localSign = sign(data, secretKey);
        return sign.equals(localSign);
    }

    public static void main(String[] args) {
        try {
            String data = "1671611401775" + 1 + "BTC-USDT-SWAP";
            String secretKey = "XCIlgPP7AIIVc5v5oitYAfe5pAVXqhWfnd1p9jvDELuMZyOzCqO03z9X5nvIKa8eKBtPMFhSS7ggf0TqOmJ7yUU0JucaveGhVEAsANGWCcunOEd35rsXBgWB38HdsN8y";
            System.out.println(sign(data, secretKey));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
