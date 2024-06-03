package kol.account.dto.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author kent
 */
@Data
@ApiModel("钱包信息")
public class BindWalletAddrCmd {
    @ApiModelProperty("钱包地址")
    @NotBlank(message = "walletAddr不能为空")
    private String walletAddr;
}