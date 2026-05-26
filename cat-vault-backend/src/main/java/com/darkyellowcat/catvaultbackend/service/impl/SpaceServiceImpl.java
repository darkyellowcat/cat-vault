package com.darkyellowcat.catvaultbackend.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darkyellowcat.catvaultbackend.exception.BusinessException;
import com.darkyellowcat.catvaultbackend.exception.ErrorCode;
import com.darkyellowcat.catvaultbackend.exception.ThrowUtils;
import com.darkyellowcat.catvaultbackend.mapper.SpaceMapper;
import com.darkyellowcat.catvaultbackend.model.dto.space.SpaceAddRequest;
import com.darkyellowcat.catvaultbackend.model.dto.space.SpaceQueryRequest;
import com.darkyellowcat.catvaultbackend.model.entity.Space;
import com.darkyellowcat.catvaultbackend.model.entity.SpaceUser;
import com.darkyellowcat.catvaultbackend.model.entity.User;
import com.darkyellowcat.catvaultbackend.model.enums.SpaceLevelEnum;
import com.darkyellowcat.catvaultbackend.model.enums.SpaceRoleEnum;
import com.darkyellowcat.catvaultbackend.model.enums.SpaceTypeEnum;
import com.darkyellowcat.catvaultbackend.model.enums.UserRoleEnum;
import com.darkyellowcat.catvaultbackend.model.vo.SpaceVO;
import com.darkyellowcat.catvaultbackend.model.vo.UserVO;
import com.darkyellowcat.catvaultbackend.service.SpaceService;
import com.darkyellowcat.catvaultbackend.service.SpaceUserService;
import com.darkyellowcat.catvaultbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private SpaceUserService spaceUserService;

    private static final int FREE_USER_MAX_SPACES = 3;
    private static final int VIP_USER_MAX_SPACES = 10;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        Space space = new Space();
        space.setSpaceName(spaceAddRequest.getSpaceName());
        space.setSpaceLevel(spaceAddRequest.getSpaceLevel());
        if (spaceAddRequest.getSpaceType() != null) {
            space.setSpaceType(spaceAddRequest.getSpaceType());
        } else {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 权限校验：普通用户只能创建免费版，付费用户可创建任意级别
        int vipLevel = loginUser.getVipLevel() != null ? loginUser.getVipLevel() : 0;
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        if (!isAdmin) {
            if (vipLevel == 0) {
                ThrowUtils.throwIf(spaceAddRequest.getSpaceLevel() != 0,
                        ErrorCode.NO_AUTH_ERROR, "普通用户只能创建免费版空间，请升级会员");
                long existCount = this.lambdaQuery()
                        .eq(Space::getUserId, loginUser.getId())
                        .count();
                ThrowUtils.throwIf(existCount >= FREE_USER_MAX_SPACES,
                        ErrorCode.OPERATION_ERROR, "免费用户最多创建 " + FREE_USER_MAX_SPACES + " 个空间");
            } else {
                long existCount = this.lambdaQuery()
                        .eq(Space::getUserId, loginUser.getId())
                        .count();
                ThrowUtils.throwIf(existCount >= VIP_USER_MAX_SPACES,
                        ErrorCode.OPERATION_ERROR, "已达空间数量上限（" + VIP_USER_MAX_SPACES + " 个）");
            }
        }
        // 填充容量
        fillSpaceBySpaceLevel(space);
        space.setUserId(loginUser.getId());
        // 校验
        validSpace(space, true);
        boolean result = this.save(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");
        // 将创建者加入空间成员（admin 角色）
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(space.getId());
        spaceUser.setUserId(loginUser.getId());
        spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
        spaceUserService.save(spaceUser);
        return space.getId();
    }

    @Override
    public void validSpace(Space space, boolean isAdd) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        Integer spaceType = space.getSpaceType();

        if (isAdd) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR, "空间级别不能为空");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        if (spaceLevel != null && SpaceLevelEnum.getEnumByValue(spaceLevel) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (spaceType != null && SpaceTypeEnum.getEnumByValue(spaceType) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不存在");
        }
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            if (space.getMaxSize() == null || space.getMaxSize() == 0) {
                space.setMaxSize(spaceLevelEnum.getMaxSize());
            }
            if (space.getMaxCount() == null || space.getMaxCount() == 0) {
                space.setMaxCount(spaceLevelEnum.getMaxCount());
            }
        }
        if (space.getTotalSize() == null) {
            space.setTotalSize(0L);
        }
        if (space.getTotalCount() == null) {
            space.setTotalCount(0L);
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        Long userId = spaceQueryRequest.getUserId();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);

        if (StrUtil.isNotEmpty(sortField)) {
            java.util.List<String> validSortFields = java.util.Arrays.asList(
                    "id", "spaceName", "spaceLevel", "totalSize", "totalCount", "createTime", "updateTime");
            ThrowUtils.throwIf(!validSortFields.contains(sortField), ErrorCode.PARAMS_ERROR, "非法排序字段");
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), sortField);
        }
        return queryWrapper;
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public void checkSpaceCapacity(Space space, long addSize) {
        ThrowUtils.throwIf(space.getTotalCount() >= space.getMaxCount(),
                ErrorCode.OPERATION_ERROR, "空间图片数量已达上限");
        ThrowUtils.throwIf(space.getTotalSize() + addSize > space.getMaxSize(),
                ErrorCode.OPERATION_ERROR, "空间容量不足");
    }
}
