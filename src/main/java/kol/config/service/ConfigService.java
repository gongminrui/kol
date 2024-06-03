package kol.config.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import kol.account.model.RoleEnum;
import kol.common.annotation.NotLogRecord;
import kol.common.service.BaseService;
import kol.config.model.Config;
import kol.config.model.Config.KeyEnum;
import kol.config.repo.ConfigRepo;

/**
 * @author Gongminrui
 * @date 2023-03-20 23:02
 */
@Service
public class ConfigService extends BaseService {
    @Resource
    private ConfigRepo configRepo;

    private Map<Config.KeyEnum, String> cacheMaps = new HashMap<>();


    @PostConstruct
    public void init() {
        Map<Config.KeyEnum, String> map = configRepo.findAll().stream()
                .collect(Collectors.toMap(Config::getK, Config::getV));
        if (CollectionUtils.isEmpty(map)) {
            return;
        }

        cacheMaps.putAll(map);
    }

    public void refresh() {
        Assert.isTrue(RoleEnum.ROLE_ADMIN == getLoginAccount().getRole(), "无权限操作");
        cacheMaps.clear();
        init();
    }

    @NotLogRecord
    public String getValue(Config.KeyEnum key) {
        return cacheMaps.getOrDefault(key, "");
    }

    /**
     * 获得跟单服务费抽取比例
     *
     * @return
     */
    @NotLogRecord
    public BigDecimal getFollowCostRatio() {
        return new BigDecimal(getValue(KeyEnum.FOLLOW_COST_RATIO));
    }

    @NotLogRecord
    public BigDecimal getVipFee() {
        String value = getValue(KeyEnum.VIP_FEE);
        if (StringUtils.isBlank(value)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    /**
     * 获得注册赠送vip天数
     *
     * @return
     */
    @NotLogRecord
    public int getRegisterGiveVipDay() {
        String value = getValue(KeyEnum.REGISTER_GIVE_VIP_DAY);
        if (StringUtils.isBlank(value)) {
            return 0;
        }
        return Integer.valueOf(value);
    }

    /**
     * 距离会员到期提醒天数
     *
     * @return
     */
    public int getVipExpireWarnDay() {
        String value = getValue(KeyEnum.VIP_EXPIRE_WARN_DAY);
        if (StringUtils.isBlank(value)) {
            return 1;
        }
        return Integer.valueOf(value);
    }

    /**
     * 是否账单净值模式
     *
     * @return
     */
    @NotLogRecord
    public boolean isBillNetValue() {
        String value = getValue(KeyEnum.BILL_NET_VALUE);
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return Boolean.valueOf(value);
    }

    /**
     * 获得平台名称
     *
     * @return
     */
    public String getPlatformName() {
        return getValue(KeyEnum.PLATFORM_NAME);
    }

    /**
     * 邮件发件人
     *
     * @return
     */
    public String getEmailFrom() {
        String value = getValue(KeyEnum.EMAIL_FROM);
        if (StringUtils.isBlank(value)) {
            return "noreply@glodfox.com";
        }
        return value;
    }

    /**
     * 最小结算金额
     *
     * @return
     */
    @NotLogRecord
    public BigDecimal getMinSettleAmount() {
        String value = getValue(KeyEnum.MIN_SETTLE_AMOUNT);
        if (StringUtils.isBlank(value)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    @Transactional(rollbackFor = Exception.class)
    public void set(KeyEnum k, String v, String comment) {
        configRepo.findById(k).ifPresentOrElse(c -> {
            c.setK(k);
            c.setComment(comment);
            c.setV(v);
            configRepo.save(c);
        }, () -> {
            Config c = new Config();
            c.setK(k);
            c.setComment(comment);
            c.setV(v);
            configRepo.save(c);
        });
    }
}
