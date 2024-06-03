package kol.vcode.model;

import java.time.Duration;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import kol.common.utils.EmailTool;
import lombok.Getter;

/**
 * @author guan
 */
@Service
public class VcodeService {
    @Resource
    EmailTool emailTool;
    @Getter
    Cache<String, String> vcodeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10)).build();

    /**
     * 发送验证码
     *
     * @param email
     */
    public void send(String email) {
        String vcode = RandomStringUtils.random(6, false, true);
        vcodeCache.put(email, vcode);
        emailTool.sendMessage(email, "验证码", String.format("您的验证码为 %s ，10分钟内有效。", vcode));
    }

    /**
     * 验证
     *
     * @param email
     * @param vcode
     * @return true 验证通过 false 验证失败
     */
    public boolean verify(String email, String vcode) {
        String cachedVcode = vcodeCache.getIfPresent(email);
        return cachedVcode != null && cachedVcode.equalsIgnoreCase(vcode);
    }
}
