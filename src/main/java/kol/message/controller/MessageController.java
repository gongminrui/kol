package kol.message.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kol.common.model.*;
import kol.message.dto.cmd.MessageAddCmd;
import kol.message.model.Message;
import kol.message.service.MessageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author Gongminrui
 * @date 2023-03-03 10:37
 */
@RestController
@Api(tags = "消息中心")
@RequestMapping("/api/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @GetMapping("getMessage")
    @ApiOperation("获得消息列表")
    public ArrayResponse<Message> getMessage() {
        return ArrayResponse.of(messageService.listByAccountId());
    }


    @GetMapping("getPageMessage")
    @ApiOperation("获得翻页消息")
    public PageResponse<Message> getPageMessage(@RequestParam("pageIndex") int pageIndex, @RequestParam("pageSize") int pageSize) {
        return messageService.pageByAccountId(pageIndex, pageSize);
    }

    @PostMapping("setRead")
    @ApiOperation("设置已读")
    public Response setRead(@RequestBody @Validated BaseDTO baseDTO) {
        messageService.setRead(baseDTO);
        return Response.buildSuccess();
    }

    @GetMapping("getUnreadCount")
    @ApiOperation("获得未读消息数量")
    public SingleResponse<Integer> getUnreadCount() {
        return SingleResponse.of(messageService.getUnreadCount());
    }

    @PostMapping("/save/all/account")
    @ApiOperation("给所有用户法消息")
    public Response saveMessageAllAccount(@RequestBody @Valid MessageAddCmd cmd) {
        messageService.saveMessageAllAccount(cmd);
        return Response.buildSuccess();
    }
}
