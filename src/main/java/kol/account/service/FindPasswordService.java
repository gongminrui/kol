package kol.account.service;

import kol.account.model.Account;
import kol.account.repo.AccountRepo;
import kol.common.model.AppException;
import kol.vcode.model.VcodeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author guan
 */
@Service
public class FindPasswordService {
    final AccountRepo accountRepo;
    final PasswordEncoder passwordEncoder;
    final VcodeService vcodeService;

    public FindPasswordService(AccountRepo accountRepo, PasswordEncoder passwordEncoder, VcodeService vcodeService) {
        this.accountRepo = accountRepo;
        this.passwordEncoder = passwordEncoder;
        this.vcodeService = vcodeService;
    }

    /**
     * 找回密码
     *
     * @param vcode
     * @param newPassword
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public void findPassword(String vcode, String newPassword) {
        String email = vcodeService.getVcodeCache().getIfPresent(vcode);
        if (email == null) {
            throw new AppException("VCODE_ERROR", "验证码错误");
        }
        Account account =
                accountRepo
                        .findByEmail(email)
                        .orElseThrow(() -> new AppException("USER_NOT_FOUND", "用户不存在"));
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepo.save(account);
    }
}
