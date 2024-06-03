package kol.account.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import kol.account.model.RoleEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author kent
 */
@Data
@ApiModel("合伙人")
public class AgentVO {
    @ApiModelProperty("成为合伙人账号ID")
    private Long accountId;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("邀请码")
    private String inviteCode;
    @ApiModelProperty("返佣比例（按交易所返佣比例设计）")
    private BigDecimal rebate;
    @ApiModelProperty("返佣金额")
    private BigDecimal rebateAmount;
    @ApiModelProperty("邀请人数")
    private Integer inviteCount;
    @ApiModelProperty("跟单资金")
    private BigDecimal followAmount;
    @ApiModelProperty("角色")
    private RoleEnum role;
}
