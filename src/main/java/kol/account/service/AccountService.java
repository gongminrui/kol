package kol.account.service;

import kol.account.dto.cmd.UpdateStatusCmd;
import kol.account.dto.vo.AccountVO;
import kol.account.model.Account;
import kol.account.model.AccountRebate;
import kol.account.model.AccountStatusEnum;
import kol.account.repo.AccountRepo;
import kol.common.model.AppException;
import kol.common.model.PageResponse;
import kol.common.service.BaseService;
import kol.common.utils.TimeRangeUtil;
import kol.money.model.Wallet;
import kol.money.repo.WalletRepo;
import kol.trade.repo.InvestmentRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.tron.easywork.util.TronConverter;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author kent
 */
@Service
public class AccountService extends BaseService {
    final AccountRepo accountRepo;
    final InvestmentRepo investmentRepo;
    final WalletRepo walletRepo;

    @Resource
    private AccountRebateService accountRebateService;

    public AccountService(AccountRepo accountRepo, InvestmentRepo investmentRepo, WalletRepo walletRepo) {
        this.accountRepo = accountRepo;
        this.investmentRepo = investmentRepo;
        this.walletRepo = walletRepo;
    }

    public Optional<Account> getById(Long accountId) {
        return accountRepo.findById(accountId);
    }

    public AccountVO me(HttpServletRequest request) {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 账户基本资料
        Account me = accountRepo.findById(accountId)
                .map(account -> {
                    // 记录用户最后访问ip和时间
                    account.setLastLoginIp(request.getRemoteAddr());
                    account.setLastLoginTime(LocalDateTime.now());
                    //地址统一转base58
                    if(account.getTrxAddress()!=null&&account.getTrxAddress().startsWith("41")) {
                    	String base58=TronConverter.hexToBase58(account.getTrxAddress());
                    	account.setTrxAddress(base58);
                    }
                    accountRepo.save(account);
                    return account;
                })
                .orElseThrow(() -> new AppException("ACCOUNT_NOT_FOUND", "账户不存在"));

        AccountVO vo = new AccountVO();
        BeanUtils.copyProperties(me, vo);

        //平台余额
        Optional<Wallet> walletOptional = walletRepo.findByAccountId(accountId);

        Assert.isTrue(walletOptional.isPresent(), "你的账号数据异常，请联系管理员");
        Wallet wallet = walletOptional.get();
        vo.setBalance(wallet.getBalance());

        List<Map<String, BigDecimal>> mapList = investmentRepo.findInvestor(accountId);
        BigDecimal profit = Optional.ofNullable(mapList.get(0).get("profit")).orElse(BigDecimal.ZERO);
        //总利润
        vo.setProfit(profit);
        //跟单资金
        BigDecimal principal = Optional.ofNullable(mapList.get(0).get("principal")).orElse(BigDecimal.ZERO);
        vo.setPrincipal(principal);
        return vo;
    }

    /**
     * 查询用户列表
     *
     * @param accountId
     * @param email
     * @param inviteCode
     * @return
     */
    public PageResponse<AccountVO> getList(Long accountId, String email, String inviteCode, int pageIndex, int pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pageIndex - 1, pageSize, sort);

        Specification spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (StringUtils.isNotBlank(email)) {
                predicateList.add(criteriaBuilder.equal(root.get("email"), email));
            }
            if (StringUtils.isNotBlank(inviteCode)) {
                predicateList.add(criteriaBuilder.equal(root.get("inviteCode"), inviteCode));
            }
            if (Objects.nonNull(accountId)) {
                predicateList.add(criteriaBuilder.equal(root.get("id"), accountId));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
        };
        Page<Account> page = accountRepo.findAll(spec, pageRequest);
        List<Account> content = page.getContent();

        List<AccountVO> list = new ArrayList<>();
        if (!content.isEmpty()) {
            List<Long> accountIds = content.stream().map(Account::getId).toList();
            List<AccountRebate> accountRebateList = accountRebateService.findByAccountIdIn(accountIds);

            for (Account account : content) {
                AccountVO accountVO = new AccountVO();
                BeanUtils.copyProperties(account, accountVO);

                Optional<AccountRebate> first = accountRebateList.stream()
                        .filter(v -> v.getAccountId().equals(account.getId())).findFirst();
                first.ifPresent(v -> accountVO.setRebate(v.getRebate()));
                list.add(accountVO);
            }
        }

        return PageResponse.of(list, page.getTotalElements(), pageIndex, pageSize);
    }

    public List<Account> listNextAccount(Long pid) {
        return accountRepo.listNext(pid);
    }

    public List<Account> findAll() {
        return accountRepo.findAll();
    }

    /**
     * 是否禁用
     *
     * @param account
     * @return
     */
    public boolean isDisable(Account account) {
        return AccountStatusEnum.DISABLE == account.getStatus();
    }

    /**
     * 是否禁用
     *
     * @param accountId
     * @return
     */
    public boolean isDisable(Long accountId) {
        return isDisable(accountRepo.findById(accountId).get());
    }

    public int updateStatus(UpdateStatusCmd updateStatusCmd) {
        // 非管理员不能操作
        Assert.isTrue(isAdmin(), "无权限操作");
        return updateStatus(updateStatusCmd.getAccountId(), updateStatusCmd.getStatus());
    }

    public int updateStatus(Long accountId, AccountStatusEnum accountStatusEnum) {
        Optional<Account> optionalAccount = accountRepo.findById(accountId);
        Assert.isTrue(optionalAccount.isPresent(), "用户不存在");

        Account account = optionalAccount.get();
        account.setStatus(accountStatusEnum);
        accountRepo.save(account);
        return 1;
    }

    /**
     * 禁用用户
     *
     * @param accountId
     * @return
     */
    public int disableAccount(Long accountId) {
        return updateStatus(accountId, AccountStatusEnum.DISABLE);
    }

    /**
     * 冻结账户
     *
     * @param accountId
     * @return
     */
    public int freezeAccount(Long accountId) {
        return updateStatus(accountId, AccountStatusEnum.FREEZE);
    }

    /**
     * 恢复为正常用户
     *
     * @param accountId
     * @return
     */
    public int normalAccount(Long accountId) {
        return updateStatus(accountId, AccountStatusEnum.NORMAL);
    }

    /**
     * 根据VIP等级获取列表
     *
     * @param vipLevel
     * @return
     */
    public List<Account> findByVipLevel(Integer vipLevel) {
        return accountRepo.findByVipLevel(vipLevel);
    }

    /**
     * 获得会员列表
     *
     * @return
     */
    public List<Account> getVipList() {
        return accountRepo.findByVipLevelGreaterThanEqual(1);
    }

    public int openVip(Long accountId) {
        Optional<Account> optionalAccount = accountRepo.findById(accountId);
        if (optionalAccount.isEmpty()) {
            return -1;
        }
        Account account = optionalAccount.get();
        account.setVipLevel(1);
        account.setVipCount(account.getVipCount() + 1);
        account.setVipExpired(TimeRangeUtil.getNextMonthFirst());
        accountRepo.save(account);
        return 1;
    }

    public int stopVip(Long accountId) {
        Optional<Account> optionalAccount = accountRepo.findById(accountId);
        if (optionalAccount.isEmpty()) {
            return -1;
        }
        Account account = optionalAccount.get();
        account.setVipLevel(0);
        accountRepo.save(account);
        return 1;
    }
}
