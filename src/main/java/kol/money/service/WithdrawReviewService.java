package kol.money.service;

import kol.account.model.Account;
import kol.account.model.RoleEnum;
import kol.account.repo.AccountRepo;
import kol.account.service.AccountService;
import kol.common.model.ErrorMsg;
import kol.common.model.MessageType;
import kol.common.model.PageResponse;
import kol.common.service.BaseService;
import kol.money.dto.cmd.HandleWithdrawCmd;
import kol.money.dto.cmd.SelectWithdrawCmd;
import kol.money.dto.cmd.WithdrawCmd;
import kol.money.model.Wallet;
import kol.money.model.WithdrawReview;
import kol.money.repo.WalletRepo;
import kol.money.repo.WithdrawReviewRepo;
import kol.trade.service.InvestmentService;
import kol.vcode.model.VcodeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Gongminrui
 * @date 2023-03-03 20:30
 */
@Service
@Slf4j
public class WithdrawReviewService extends BaseService {
    @Resource
    private WithdrawReviewRepo withdrawReviewRepo;
    @Resource
    private WalletRepo walletRepo;
    @Resource
    private AccountRepo accountRepo;
    @Resource
    private AccountService accountService;
    @Resource
    private WalletService walletService;
    @Resource
    private VcodeService vcodeService;
    @Resource
    private InvestmentService investmentService;

    @Value("${spring.profiles.active}")
    private String active;

    public void sendWithdrawVerify() {
        Account loginAccount = getLoginAccount();
        vcodeService.send(loginAccount.getEmail());
    }

    /**
     * 申请提现
     *
     * @param withdrawCmd
     * @return
     */
    public int applyWithdraw(WithdrawCmd withdrawCmd) {
        Account loginAccount = getLoginAccount();
        Long accountId = loginAccount.getId();
        // 生产环境要验证码校验
        if ("pro".equals(active)) {
            Assert.isTrue(vcodeService.verify(loginAccount.getEmail(), withdrawCmd.getVcode()), "验证码不正确");
        }

        Assert.isTrue(!accountService.isDisable(loginAccount), MessageType.ACCOUNT_DISABLE.getZh());

        // 提现金额
        final BigDecimal withdrawAmount = withdrawCmd.getAmount();
        final Account account = accountRepo.findById(accountId).get();
//        String walletAddr = account.getWalletAddr();
        Assert.isTrue(StringUtils.isNotBlank(withdrawCmd.getAddr()), "请设置提现账号");

        boolean hasInvertment = investmentService.hasInvertment(accountId);
        Assert.isTrue(!hasInvertment, ErrorMsg.IN_INVESTMENT.getMsg());

        // 获取当前余额
        BigDecimal balance = BigDecimal.ZERO;
        Optional<Wallet> optional = walletRepo.findByAccountId(accountId);
        if (optional.isPresent()) {
            Wallet wallet = optional.get();
            balance = balance.add(wallet.getBalance());
//            // 跟单服务费
//            BigDecimal followCostAmount = wallet.getFollowCostAmount();
//            // 赠送金额
//            BigDecimal giveAmount = wallet.getGiveAmount();
//            // 如果跟单服务器小于赠送金额
//            if (followCostAmount.compareTo(giveAmount) < 0) {
//                balance = balance.subtract(giveAmount.subtract(followCostAmount));
//            }

            // 减掉赠送金额就是可提现的金额
            BigDecimal giveAmount = wallet.getGiveAmount();
            if (giveAmount.compareTo(BigDecimal.ZERO) > 0) {
                balance = balance.subtract(giveAmount);
            }
        }
        Assert.isTrue(balance.compareTo(withdrawAmount) >= 0, ErrorMsg.NOT_BALANCE.getMsg());

        String lockStr = "applyWithdraw" + account;
        synchronized (lockStr.intern()) {
            // 还有未审核的提现申请，不能重复申请
            int count = withdrawReviewRepo.countByAccountIdAndReview(accountId, false);
            Assert.isTrue(count <= 0, ErrorMsg.RETRY_SUBMIT_WITHDRAW_REVIEW.getCode());

            WithdrawReview withdrawReview = new WithdrawReview();
            withdrawReview.setAccountId(accountId);
            withdrawReview.setEmail(account.getEmail());
            withdrawReview.setWalletAddr(withdrawCmd.getAddr());
            withdrawReview.setWalletAmount(withdrawAmount);
            withdrawReview.setReview(false);
            withdrawReview.setWithdrawAddr(withdrawCmd.getAddr());
            withdrawReviewRepo.save(withdrawReview);
        }
        return 1;
    }


    /**
     * 处理提现申请
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int handleApply(HandleWithdrawCmd handleWithdrawCmd) {
        Optional<WithdrawReview> optional = withdrawReviewRepo.findById(handleWithdrawCmd.getId());
        Assert.isTrue(optional.isPresent(), "提现申请不存在");

        Boolean pass = handleWithdrawCmd.getPass();
        WithdrawReview withdrawReview = optional.get();
        withdrawReview.setReview(true);
        withdrawReview.setPass(pass);
        withdrawReview.setRemarks(handleWithdrawCmd.getRemarks());
        withdrawReviewRepo.save(withdrawReview);
        if (pass) {
            // 申请成功之后就把余额扣下来
            walletService.cashOut(withdrawReview.getAccountId(), withdrawReview.getWalletAmount());
        } else {
//            sendMessage(withdrawReview.getAccountId(), "提现失败", withdrawReview.getRemarks());
            sendMessage(withdrawReview.getAccountId(), MessageType.WITHDRAW_FAILD, null);
        }
        return 1;
    }

    /**
     * 获得提现审核翻页数据
     *
     * @param selectWithdrawCmd
     * @return
     */
    public PageResponse<WithdrawReview> pageWithdrawReview(SelectWithdrawCmd selectWithdrawCmd) {
        int pageIndex = selectWithdrawCmd.getPageIndex();
        int pageSize = selectWithdrawCmd.getPageSize();

        Boolean review = selectWithdrawCmd.getReview();
        Boolean pass = selectWithdrawCmd.getPass();
        String email = selectWithdrawCmd.getEmail();

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pageIndex - 1, pageSize, sort);

        Account loginAccount = getLoginAccount();

        Specification<WithdrawReview> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (review != null) {
                list.add(criteriaBuilder.equal(root.get("review"), review));
            }
            if (pass != null) {
                list.add(criteriaBuilder.equal(root.get("pass"), pass));
            }
            if (StringUtils.isNotBlank(email)) {
                list.add(criteriaBuilder.like(root.get("email").as(String.class), "%" + email + "%"));
            }
            if (RoleEnum.ROLE_ADMIN != loginAccount.getRole()) {
                list.add(criteriaBuilder.equal(root.get("accountId"), loginAccount.getId()));
            }
            return criteriaBuilder.and(list.toArray(new Predicate[0]));
        };

        Page<WithdrawReview> page = withdrawReviewRepo.findAll(specification, pageRequest);
        return PageResponse.of(page.getContent(), page.getTotalElements(), pageIndex, pageSize);
    }
}
