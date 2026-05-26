package com.darkyellowcat.catvaultbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.darkyellowcat.catvaultbackend.common.BaseResponse;
import com.darkyellowcat.catvaultbackend.common.ResultUtils;
import com.darkyellowcat.catvaultbackend.exception.ErrorCode;
import com.darkyellowcat.catvaultbackend.exception.ThrowUtils;
import com.darkyellowcat.catvaultbackend.mapper.MessageMapper;
import com.darkyellowcat.catvaultbackend.model.entity.Message;
import com.darkyellowcat.catvaultbackend.model.entity.User;
import com.darkyellowcat.catvaultbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private UserService userService;

    @GetMapping("/my/list")
    public BaseResponse<List<Message>> listMyMessages(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.orderByDesc("createTime");
        queryWrapper.last("LIMIT 50");
        List<Message> messages = messageMapper.selectList(queryWrapper);
        return ResultUtils.success(messages);
    }

    @GetMapping("/my/unread/count")
    public BaseResponse<Long> getUnreadCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.eq("hasRead", 0);
        Long count = messageMapper.selectCount(queryWrapper);
        return ResultUtils.success(count);
    }

    @PostMapping("/read")
    public BaseResponse<Boolean> markAsRead(@RequestBody List<Long> messageIds, HttpServletRequest request) {
        ThrowUtils.throwIf(messageIds == null || messageIds.isEmpty(), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        UpdateWrapper<Message> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", messageIds);
        updateWrapper.eq("userId", loginUser.getId());
        updateWrapper.set("hasRead", 1);
        messageMapper.update(null, updateWrapper);
        return ResultUtils.success(true);
    }
}
