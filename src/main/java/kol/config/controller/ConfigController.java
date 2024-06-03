package kol.config.controller;

import kol.common.model.ArrayResponse;
import kol.common.model.Response;
import kol.config.service.ConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import kol.common.model.AppException;
import kol.common.model.SingleResponse;
import kol.config.model.Config;
import kol.config.repo.ConfigRepo;
import lombok.Data;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;

/**
 * @author guan
 */
@Api(tags = "配置模块")
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Resource
    private ConfigService configService;
    @Resource
    private ConfigRepo configRepo;


    @ApiOperation("配置信息")
    @GetMapping("/get")
    public SingleResponse<Config> get(RequestParams request) {
        Config config = configRepo.findById(Config.KeyEnum.valueOf(request.getKey()))
                .orElseThrow(() -> new AppException("CONFIG_NOT_FOUND", "配置项不存在"));
        return SingleResponse.of(config);
    }

    @ApiOperation("配置信息")
    @GetMapping("/getList")
    public ArrayResponse<Config> getList(RequestParams request) {
        String[] keys = request.getKey().split(",");
        List<Config.KeyEnum> keyEnums = Arrays.stream(keys).map(Config.KeyEnum::valueOf).toList();
        return ArrayResponse.of(configRepo.findByKIn(keyEnums));
    }

    @ApiOperation("刷新配置")
    @GetMapping("refresh")
    public Response refresh() {
        configService.refresh();
        return Response.buildSuccess();
    }

    @Data
    public static class RequestParams {
        @ApiModelProperty("CHAIN_TRC20_ADDR-平台充值地址，TERMS_OF_SERVICE-服务协议")
        @NotBlank(message = "key不能为空")
        private String key;
    }

}
