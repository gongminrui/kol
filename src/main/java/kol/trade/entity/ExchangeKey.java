package kol.trade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kol.common.model.BaseEntity;
import kol.trade.enums.ExchangeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;

/**
 * 交易所的KEY
 *
 * @author guanzhenggang@gmail.com
 */

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "trade_key")
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeKey extends BaseEntity {
    /**
     * 账号ID
     */
    @Column(nullable = false)
    private Long accountId;

    /**
     * key的名称
     */
    @Column(nullable = false, length = 20)
    private String name;

    /**
     * apiKey值
     */
    @Column(nullable = false, length = 256, unique = true)
    private String apiKey;

    /**
     * 密钥
     */
    @Column(nullable = false, length = 256)
    private String secretKey;

    /**
     * 创建API时输入的密码
     */
    @Column(length = 100)
    private String passphrase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExchangeEnum exchange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExchangeKeyStatusEnum status = ExchangeKeyStatusEnum.NORMAL;

    public enum ExchangeKeyStatusEnum {
        NORMAL,
        DELETE
    }
}
