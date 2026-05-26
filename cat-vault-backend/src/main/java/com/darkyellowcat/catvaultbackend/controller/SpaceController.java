package com.darkyellowcat.catvaultbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.darkyellowcat.catvaultbackend.annotation.AuthCheck;
import com.darkyellowcat.catvaultbackend.common.BaseResponse;
import com.darkyellowcat.catvaultbackend.common.DeleteRequest;
import com.darkyellowcat.catvaultbackend.common.ResultUtils;
import com.darkyellowcat.catvaultbackend.constant.UserConstant;
import com.darkyellowcat.catvaultbackend.exception.BusinessException;
import com.darkyellowcat.catvaultbackend.exception.ErrorCode;
import com.darkyellowcat.catvaultbackend.exception.ThrowUtils;
import com.darkyellowcat.catvaultbackend.model.dto.space.SpaceAddRequest;
import com.darkyellowcat.catvaultbackend.model.dto.space.SpaceQueryRequest;
import com.darkyellowcat.catvaultbackend.model.dto.space.SpaceUpdateRequest;
import com.darkyellowcat.catvaultbackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.darkyellowcat.catvaultbackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.darkyellowcat.catvaultbackend.model.entity.Space;
import com.darkyellowcat.catvaultbackend.model.entity.SpaceUser;
import com.darkyellowcat.catvaultbackend.model.entity.User;
import com.darkyellowcat.catvaultbackend.model.enums.SpaceRoleEnum;
import com.darkyellowcat.catvaultbackend.model.vo.SpaceUserVO;
import com.darkyellowcat.catvaultbackend.model.vo.SpaceVO;
import com.darkyellowcat.catvaultbackend.service.SpaceService;
import com.darkyellowcat.catvaultbackend.service.SpaceUserService;
import com.darkyellowcat.catvaultbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import com.darkyellowcat.catvaultbackend.mapper.MessageMapper;
import com.darkyellowcat.catvaultbackend.model.entity.Message;

import java.util.List;

@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    @Resource
    private MessageMapper messageMapper;

    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long spaceId = spaceService.addSpace(spaceAddRequest, loginUser);
        // 通知创建者
        Message msg = new Message();
        msg.setUserId(loginUser.getId());
        msg.setTitle("空间创建成功");
        msg.setContent("您的空间「" + spaceAddRequest.getSpaceName() + "」已创建成功");
        msg.setHasRead(0);
        messageMapper.insert(msg);
        return ResultUtils.success(spaceId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUpdateRequest == null || spaceUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        spaceService.validSpace(space, false);
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceService.getSpaceVO(space, request));
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                          HttpServletRequest request) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        Page<SpaceVO> spaceVOPage = new Page<>(current, size, spacePage.getTotal());
        List<SpaceVO> spaceVOList = spacePage.getRecords().stream()
                .map(space -> spaceService.getSpaceVO(space, request))
                .toList();
        spaceVOPage.setRecords(spaceVOList);
        return ResultUtils.success(spaceVOPage);
    }

    // ========== 空间成员管理 ==========

    @PostMapping("/member/add")
    public BaseResponse<Boolean> addSpaceMember(@RequestBody SpaceUserAddRequest spaceUserAddRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long spaceId = spaceUserAddRequest.getSpaceId();
        Long userId = spaceUserAddRequest.getUserId();
        ThrowUtils.throwIf(spaceId == null || userId == null, ErrorCode.PARAMS_ERROR);
        // 校验空间存在
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 仅空间创建者或管理员可添加成员
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 校验目标用户存在
        User targetUser = userService.getById(userId);
        ThrowUtils.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        // 校验是否已是成员
        long count = spaceUserService.lambdaQuery()
                .eq(SpaceUser::getSpaceId, spaceId)
                .eq(SpaceUser::getUserId, userId)
                .count();
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "该用户已是空间成员");
        // 添加成员
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(userId);
        String spaceRole = spaceUserAddRequest.getSpaceRole();
        if (spaceRole == null || SpaceRoleEnum.getEnumByValue(spaceRole) == null) {
            spaceRole = SpaceRoleEnum.VIEWER.getValue();
        }
        spaceUser.setSpaceRole(spaceRole);
        boolean result = spaceUserService.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 通知被添加的用户
        Message msg = new Message();
        msg.setUserId(userId);
        msg.setTitle("您已被添加到空间");
        msg.setContent("您已被添加到空间「" + space.getSpaceName() + "」，角色：" + spaceRole);
        msg.setHasRead(0);
        messageMapper.insert(msg);
        return ResultUtils.success(true);
    }

    @PostMapping("/member/remove")
    public BaseResponse<Boolean> removeSpaceMember(@RequestBody SpaceUserAddRequest spaceUserAddRequest,
                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long spaceId = spaceUserAddRequest.getSpaceId();
        Long userId = spaceUserAddRequest.getUserId();
        ThrowUtils.throwIf(spaceId == null || userId == null, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 禁止移除空间创建者
        if (userId.equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能移除空间创建者");
        }
        boolean result = spaceUserService.lambdaUpdate()
                .eq(SpaceUser::getSpaceId, spaceId)
                .eq(SpaceUser::getUserId, userId)
                .remove();
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 通知被移除的用户
        Message msg = new Message();
        msg.setUserId(userId);
        msg.setTitle("您已被移出空间");
        msg.setContent("您已被移出空间「" + space.getSpaceName() + "」");
        msg.setHasRead(0);
        messageMapper.insert(msg);
        return ResultUtils.success(true);
    }

    @PostMapping("/member/list")
    public BaseResponse<List<SpaceUserVO>> listSpaceMembers(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest,
                                                             HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null || spaceUserQueryRequest.getSpaceId() == null,
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        // 仅空间成员或管理员可查看成员列表
        if (!userService.isAdmin(loginUser)) {
            long count = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceId)
                    .eq(SpaceUser::getUserId, loginUser.getId())
                    .count();
            ThrowUtils.throwIf(count == 0, ErrorCode.NO_AUTH_ERROR, "非空间成员无权查看");
        }
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList, request));
    }

    @GetMapping("/my/list")
    public BaseResponse<List<SpaceUserVO>> listMySpaces(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<SpaceUser> spaceUserList = spaceUserService.lambdaQuery()
                .eq(SpaceUser::getUserId, loginUser.getId())
                .list();
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList, request));
    }
}
