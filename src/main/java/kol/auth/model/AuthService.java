package kol.auth.model;

import kol.account.model.Account;
import kol.account.model.AccountStatusEnum;
import kol.account.model.ClientType;
import kol.account.model.RoleEnum;
import kol.account.repo.AccountRepo;
import kol.common.cache.GlobalCache;
import kol.common.model.AppException;
import kol.common.model.PasswordConfig;
import kol.common.utils.AESUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author guan
 */
@Service
public class AuthService {

    final AccountRepo accountRepo;
    final PasswordEncoder passwordEncoder;
    final PasswordConfig passwordConfig;

    public AuthService(AccountRepo accountRepo, PasswordEncoder passwordEncoder, PasswordConfig passwordConfig) {
        this.accountRepo = accountRepo;
        this.passwordEncoder = passwordEncoder;
        this.passwordConfig = passwordConfig;
    }

    /**
     * 登录
     *
     * @param email    邮箱
     * @param password 密码
     * @param ip       ip地址
     * @return
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String login(String email, String password, String ip, String clientType) {
        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException("USERNAME_NOT_FOUND", "用户名不存在"));
        boolean result = AccountStatusEnum.DISABLE.equals(account.getStatus());
        Assert.isTrue(!result, "账号已禁用无法登录，请联系管理员");

        ClientType type = ClientType.of(clientType);

        switch (type) {
            case NOT:
                throw new AppException("NOT_LOGIN", "非法客户端");
            case CLIENT:
                if (RoleEnum.ROLE_ADMIN == account.getRole()) {
                    throw new AppException("NOT_LOGIN", "非普通用户不能登录");
                }
                break;
            case ADMIN:
                if (!RoleEnum.ROLE_ADMIN.equals(account.getRole())
                        && !RoleEnum.ROLE_TRADER.equals(account.getRole())) {
                    throw new AppException("NOT_LOGIN", "普通用户不能登录");
                }
            default:
                break;
        }

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new AppException("PASSWORD_ERROR", "密码错误");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        account.setLastLoginIp(ip);
        account.setLastLoginTime(LocalDateTime.now());
//        account.setToken(token);
        accountRepo.save(account);
        GlobalCache.cacheUser(token, account);

        return token;
    }

    /**
     * 退出
     *
     * @param token 已登录令牌
     */
    public void logout(String token) {
        accountRepo.findByToken(token).ifPresent(account -> {
            account.setToken(null);
            accountRepo.save(account);
        });
    }

    public String encryptToken(String token) {
        return AESUtils.encrypt(token, passwordConfig.getTokenPassword());
    }

    public String decryptToken(String encryptedToken) {
        return AESUtils.decrypt(encryptedToken, passwordConfig.getTokenPassword());
    }

    public static void main(String[] args) {
        String token = "f55ce75e1652cba0e2a2086481b07489cdcb1f1ee00013cbf7087fd85dd7a0e4eb98860fae3f398378d516035c2cbfbf";
        String v = AESUtils.decrypt(token, "6bb4837eb74329105ee4568dda7dc67e");
        System.out.println();
    }
}
