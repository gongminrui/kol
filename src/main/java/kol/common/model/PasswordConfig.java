package kol.common.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class PasswordConfig {
	/**
	 * 数据库敏感数据的AES密码，启动的时候注入，不注入使用默认
	 */
	@Value("${password.aes.data:a154c52565e9e7f94bfc08a1fe702624}")
	public String dataPassword;
	/**
	 * 网络通信令牌使用的AES密码
	 */
	@Value("${password.aes.token:6bb4837eb74329105ee4568dda7dc67e}")
	public String tokenPassword;
}
