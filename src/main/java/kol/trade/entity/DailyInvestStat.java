package kol.trade.entity;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author kent
 */
@Data
@Entity
@Table(name = "trade_daily_invest_stat")
@EqualsAndHashCode(callSuper = true)
public class DailyInvestStat extends BaseEntity {
    /**
     * 账号ID
     */
    @Column(nullable = false)
    private Long accountId;

    /**
     * 上级ID
     */
    private Long pid;

    /**
     * 账号邮箱
     */
    @Column(nullable = false, length = 30)
    private String email;

    /**
     * 当日跟单资金
     */
    @Column(nullable = false)
    private BigDecimal principal;

    /**
     * 当日盈亏
     */
    @Column(nullable = false, scale = 2)
    private BigDecimal profit;

    /**
     * 统计时间
     */
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date statDate;

    /**
     * 账号上级关系，最多两级
     */
    @Column(length = 30)
    private String accountRelation;

    /**
     * 截取账号层级关系当中的最近两级关系
     *
     * @return
     */
    public String subAccountRelation() {
        if (StringUtils.isNotBlank(this.accountRelation)) {
            int index = accountRelation.lastIndexOf("/", accountRelation.lastIndexOf("/") - 1);
            index = index < 0 ? 0 : index;
            return StringUtils.substring(accountRelation, index);
        }
        return null;
    }

}
