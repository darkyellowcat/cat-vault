package com.darkyellowcat.catvaultbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darkyellowcat.catvaultbackend.mapper.SpaceUserMapper;
import com.darkyellowcat.catvaultbackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.darkyellowcat.catvaultbackend.model.entity.Space;
import com.darkyellowcat.catvaultbackend.model.entity.SpaceUser;
import com.darkyellowcat.catvaultbackend.model.entity.User;
import com.darkyellowcat.catvaultbackend.model.vo.SpaceUserVO;
import com.darkyellowcat.catvaultbackend.model.vo.SpaceVO;
import com.darkyellowcat.catvaultbackend.model.vo.UserVO;
import com.darkyellowcat.catvaultbackend.service.SpaceService;
import com.darkyellowcat.catvaultbackend.service.SpaceUserService;
import com.darkyellowcat.catvaultbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(StrUtil.isNotBlank(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }

    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        // 填充用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            spaceUserVO.setUser(userService.getUserVO(user));
        }
        // 填充空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            spaceUserVO.setSpace(SpaceVO.objToVo(space));
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList, HttpServletRequest request) {
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 批量查询用户
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 批量查询空间
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        Map<Long, List<Space>> spaceIdMap = spaceService.listByIds(spaceIdSet).stream()
                .collect(Collectors.groupingBy(Space::getId));

        return spaceUserList.stream().map(spaceUser -> {
            SpaceUserVO vo = SpaceUserVO.objToVo(spaceUser);
            Long userId = spaceUser.getUserId();
            if (userIdMap.containsKey(userId)) {
                vo.setUser(userService.getUserVO(userIdMap.get(userId).get(0)));
            }
            Long spaceId = spaceUser.getSpaceId();
            if (spaceIdMap.containsKey(spaceId)) {
                vo.setSpace(SpaceVO.objToVo(spaceIdMap.get(spaceId).get(0)));
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
