package kol.vip.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kol.common.model.Response;
import kol.vip.service.VipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Gongminrui
 * @date 2023-05-28 20:58
 */
@RestController
@Api(tags = "会员模块")
@RequestMapping("/api/vip")
@RequiredArgsConstructor
public class VipController {
    final VipService vipService;

    @PostMapping("openVip")
    @ApiOperation("开通会员")
    public Response openVip() {
        vipService.openVip();
        return Response.buildSuccess();
    }

}
