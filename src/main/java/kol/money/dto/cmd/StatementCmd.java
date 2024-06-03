package kol.money.dto.cmd;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author Gongminrui
 * @date 2023-05-09 15:45
 */
@Data
@ApiModel(description = "账单查询参数")
public class StatementCmd {
    private String email;
    private int pageIndex;
    private int pageSize;
}
