package kol.common.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author kent
 */
@Data
@ApiModel("基础实体")
public class BaseDTO {
    @ApiModelProperty("主键ID")
    @NotNull(message = "主键ID不能为空")
    private Long id;
}
