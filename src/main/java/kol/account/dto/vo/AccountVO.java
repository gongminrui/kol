package kol.account.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import kol.account.model.RoleEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Comment;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author kent
 */
@Data
@Accessors(chain = true)
@ApiModel("账户信息")
public class AccountVO {
    @ApiModelProperty("账户id")
    private Long id;

    @ApiModelProperty("账户邮箱")
    private String email;

    @ApiModelProperty("头像地址")
    private String headImg;

    @ApiModelProperty("当前绑定的钱包地址")
    private String walletAddr;

    @ApiModelProperty("余额")
    private BigDecimal balance;

    @ApiModelProperty("总利润")
    private BigDecimal profit;

    @ApiModelProperty("总投入")
    private BigDecimal principal;

    @ApiModelProperty("用户角色")
    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @ApiModelProperty("用户的邀请码")
    private String inviteCode;

    @ApiModelProperty("最后登录时间")
    private LocalDateTime lastLoginTime;
    @ApiModelProperty("返利")
    private BigDecimal rebate = BigDecimal.ZERO;
    @ApiModelProperty("波场钱包地址")
    private String trxAddress;

    @ApiModelProperty("会员等级")
    private Integer vipLevel;
    @ApiModelProperty("会员过期时间")
    private Date vipExpired;
}