package kol.account.service;

import kol.account.dto.cmd.AgentCmd;
import kol.account.dto.vo.AgentVO;
import kol.account.model.Account;
import kol.account.model.AccountInfo;
import kol.account.model.RoleEnum;
import kol.account.repo.AccountInfoRepo;
import kol.account.repo.AccountRepo;
import kol.common.model.BaseDTO;
import kol.config.model.RebateRule;
import kol.config.repo.RebateRuleRepo;
import kol.money.model.Wallet;
import kol.money.repo.MoneyRecordRepo;
import kol.money.repo.WalletRepo;
import kol.trade.repo.InvestmentRepo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author kent
 */
@Service
public class AgentService {
    final AccountRepo accountRepo;
    final InvestmentRepo investmentRepo;
    final MoneyRecordRepo moneyRecordRepo;
    final AccountInfoRepo accountInfoRepo;
    final RebateRuleRepo rebateRuleRepo;
    final WalletRepo walletRepo;

    public AgentService(AccountRepo accountRepo, InvestmentRepo investmentRepo,
                        MoneyRecordRepo moneyRecordRepo, AccountInfoRepo accountInfoRepo,
                        RebateRuleRepo rebateRuleRepo, WalletRepo walletRepo) {
        this.accountRepo = accountRepo;
        this.investmentRepo = investmentRepo;
        this.moneyRecordRepo = moneyRecordRepo;
        this.accountInfoRepo = accountInfoRepo;
        this.rebateRuleRepo = rebateRuleRepo;
        this.walletRepo = walletRepo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(AgentCmd cmd) {
        Optional<Account> accountOptional = accountRepo.findById(cmd.getAccountId());
        Assert.isTrue(accountOptional.isPresent(), "用户不存在");
        Account account = accountOptional.get();
        boolean result = RoleEnum.ROLE_USER.equals(account.getRole());
        Assert.isTrue(result, "只有用户才能设为代理商");

        //判断登录用户角色，角色为管理员开通合伙人，角色为合伙人开通助力人
        Long loginAccId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Account> loginOptional = accountRepo.findById(loginAccId);
        Account loginAccount = loginOptional.get();

        if (RoleEnum.ROLE_PARTNER.equals(loginAccount.getRole())) {
            Optional<AccountInfo> inviterInfoOptional = accountInfoRepo.findByAccountId(loginAccount.getId());
            result = cmd.getInviteRebate().compareTo(inviterInfoOptional.get().getInviteRebate()) < 1;
            Assert.isTrue(result, "下级代理的返佣比例不能大于自身比例");
            account.setRole(RoleEnum.ROLE_HELPER);
        } else if (RoleEnum.ROLE_ADMIN.equals(loginAccount.getRole())) {
            account.setRole(RoleEnum.ROLE_PARTNER);
            BigDecimal inviteRebate = rebateRuleRepo.findMaxByType(RebateRule.TypeEnum.INVITE_REBATE_TYPE.toString());
            result = cmd.getInviteRebate().compareTo(inviteRebate) < 1;
            Assert.isTrue(result, "代理返佣比例不能大于平台规定最大比例");
        } else {
            Assert.isTrue(false, "没有开通代理人权限");
        }

        Optional<AccountInfo> accInfoOptional = accountInfoRepo.findByAccountId(cmd.getAccountId());
        if (accInfoOptional.isPresent()) {
            AccountInfo accountInfo = accInfoOptional.get();
            result = Objects.isNull(accountInfo.getHelperId()) && accountInfo.getPartnerId().equals(loginAccId);
            Assert.isTrue(result, "用户在其他代理下，无权设置为你的下级代理");
            accountInfo.setInviteRebate(cmd.getInviteRebate());
            accountInfoRepo.save(accountInfo);
        }

        accountRepo.save(account);
    }

    protected void updateRebate() {

    }

    public void cancelAgent(BaseDTO dto) {
        Optional<Account> accountOptional = accountRepo.findById(dto.getId());
        Assert.isTrue(accountOptional.isPresent(), "用户不存在");

        Optional<AccountInfo> accountInfoOptional = accountInfoRepo.findById(dto.getId());
        Assert.isTrue(accountInfoOptional.isPresent(), "用户数据错误，请联系客服");

        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


    }

    public AgentVO getInfo() {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<AccountInfo> optional = accountInfoRepo.findByAccountId(accountId);
        Assert.isTrue(optional.isPresent(), "登录账号数据错误");
        AccountInfo accountInfo = optional.get();

        AgentVO vo = new AgentVO();
        vo.setAccountId(accountInfo.getAccountId());
        vo.setInviteCode(accountInfo.getInviteCode());
        vo.setRebate(accountInfo.getInviteRebate());

        Optional<Wallet> walletOptional = walletRepo.findByAccountId(accountId);
        Assert.isTrue(walletOptional.isPresent(), "登录账户数据错误");
        vo.setRebateAmount(walletOptional.get().getRebateAmount());
        List<Long> accountIds = null;
        Optional<Account> accountOptional = accountRepo.findById(accountId);

        RoleEnum roleEnum = accountOptional.get().getRole();
        if (RoleEnum.ROLE_PARTNER.equals(roleEnum)) {
            accountIds = accountInfoRepo.findByPartnerId(accountId);
        } else if (RoleEnum.ROLE_HELPER.equals(roleEnum)) {
            accountIds = accountInfoRepo.findByHelperId(accountId);
        }

        if (CollectionUtils.isEmpty(accountIds)) {
            vo.setInviteCount(0);
            vo.setFollowAmount(BigDecimal.ZERO);
        } else {
            vo.setInviteCount(accountIds.size());
            BigDecimal followAmount = investmentRepo.getTotalPrincipal(accountIds);
            vo.setFollowAmount(followAmount);
        }

        return vo;
    }
}
