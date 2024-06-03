package kol.message.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kol.common.model.*;
import kol.message.model.Message;
import kol.message.model.Notice;
import kol.message.service.MessageService;
import kol.message.service.NoticeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Gongminrui
 * @date 2023-03-03 10:37
 */
@RestController
@Api(tags = "公告")
@RequestMapping("/api/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @GetMapping("getNotices")
    @ApiOperation("获得公告列表")
    public ArrayResponse<Notice> getNotices() {
        return ArrayResponse.of(noticeService.getNotices());
    }

    @PostMapping("addNotice")
    @ApiOperation("添加公告")
    public Response addNotice(@RequestParam("content") String content) {
        noticeService.addNotice(content);
        return Response.buildSuccess();
    }
}
