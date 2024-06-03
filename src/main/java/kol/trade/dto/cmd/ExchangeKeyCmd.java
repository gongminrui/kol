package kol.trade.dto.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import kol.trade.enums.ExchangeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author kent
 */
@Data
@ApiModel("添加交易所KEY")
public class ExchangeKeyCmd {
    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("交易所")
    @NotNull(message = "exchange不能为空")
    private ExchangeEnum exchange;

    @NotBlank(message = "apiKey不能为空")
    @ApiModelProperty("交易所apiKey")
    private String apiKey;

    @NotBlank(message = "secretKey不能为空")
    @ApiModelProperty("交易所secretKey")
    private String secretKey;

    @ApiModelProperty("交易所KEY密码")
    private String passphrase;
}
