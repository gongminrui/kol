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
public class ModifyPasswordService {
    final AccountRepo accountRepo;
    final PasswordEncoder passwordEncoder;
    final VcodeService vcodeService;

    public ModifyPasswordService(AccountRepo accountRepo, PasswordEncoder passwordEncoder, VcodeService vcodeService) {
        this.accountRepo = accountRepo;
        this.passwordEncoder = passwordEncoder;
        this.vcodeService = vcodeService;
    }

    /**
     * 修改密码
     *
     * @param accountId
     * @param oldPassword
     * @param newPassword
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public void modifyPassword(Long accountId, String oldPassword, String newPassword) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "用户不存在"));

        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new AppException("PASSWORD_ERROR", "用户密码错误");
        }

        account.setPassword(passwordEncoder.encode(newPassword));
    }
}
