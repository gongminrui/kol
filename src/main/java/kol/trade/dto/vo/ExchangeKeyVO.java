package kol.trade.dto.vo;

import kol.common.model.BaseDTO;
import kol.trade.enums.ExchangeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * @author kent
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ExchangeKeyVO extends BaseDTO {
    /**
     * 账号ID
     */
    private Long accountId;

    /**
     * key的名称
     */
    private String name;

    /**
     * apiKey值
     */
    private String apiKey;

    @Enumerated(EnumType.STRING)
    private ExchangeEnum exchange;
    private Date createdAt;
}
