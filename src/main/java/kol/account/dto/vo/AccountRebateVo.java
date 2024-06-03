package kol.account.dto.vo;

import kol.account.model.RoleEnum;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;

/**
 * @author Gongminrui
 * @date 2023-03-05 15:24
 */
public interface AccountRebateVo {
    Long getId();
    String getEmail();
    String getHeadImg();
    @Enumerated(EnumType.STRING)
    RoleEnum getRole();
    BigDecimal getRebate();
    String getInviteCode();
    BigDecimal getRebateAmount();
    BigDecimal getFollowCostAmount();
    Integer getInviteCount();
    Integer getVipLevel();
    Integer getVipCount();
}
