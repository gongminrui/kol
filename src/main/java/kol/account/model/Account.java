package kol.account.model;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Comment;

import com.fasterxml.jackson.annotation.JsonIgnore;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author guan
 */
@Entity(name = "account")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Account extends BaseEntity {
    /**
     * 头像地址
     */
    @Column(length = 200)
    private String headImg;

    /**
     * 邮箱，账户
     */
    @Column(nullable = false, length = 50, unique = true)
    private String email;

    /**
     * 登录密码
     */
    @JsonIgnore
    @Column(nullable = false, length = 256)
    private String password;

    /**
     * 账户角色 USER-用户  ADMIN-管理员 TRADER-交易员
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    /**
     * 上次登录IP
     */
    @Column(length = 20)
    private String lastLoginIp;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 自己的邀请码
     */
    @Column(nullable = false, length = 10, unique = true)
    private String inviteCode;

    /**
     * 登录令牌
     */
    @JsonIgnore
    @Column(length = 256)
    private String token;
    /**
     * 波场钱包地址
     */
    @Column(length = 256)
    private String trxAddress;
    /**
     * 波场钱包地址私钥
     */
    @JsonIgnore
    @Column(length = 256)
    private String trxPrivate;

    @Comment("vip等级")
    @Column(columnDefinition = "integer default 0")
    private Integer vipLevel = 0;

    @Comment("vip过期时间")
    private Date vipExpired;

    @Comment("账号状态")
    @Column(nullable = false, columnDefinition = "varchar(30) default 'NORMAL'")
    @Enumerated(EnumType.STRING)
    private AccountStatusEnum status = AccountStatusEnum.NORMAL;

    @Comment("VIP次数")
    @Column(columnDefinition = "integer default 0")
    private Integer vipCount;
}
