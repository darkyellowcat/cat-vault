package com.darkyellowcat.catvaultbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.darkyellowcat.catvaultbackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.darkyellowcat.catvaultbackend.model.entity.SpaceUser;
import com.darkyellowcat.catvaultbackend.model.vo.SpaceUserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface SpaceUserService extends IService<SpaceUser> {

    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList, HttpServletRequest request);
}
