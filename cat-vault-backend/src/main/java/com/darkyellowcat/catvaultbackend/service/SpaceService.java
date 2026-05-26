package com.darkyellowcat.catvaultbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.darkyellowcat.catvaultbackend.model.dto.space.SpaceAddRequest;
import com.darkyellowcat.catvaultbackend.model.dto.space.SpaceQueryRequest;
import com.darkyellowcat.catvaultbackend.model.entity.Space;
import com.darkyellowcat.catvaultbackend.model.entity.User;
import com.darkyellowcat.catvaultbackend.model.vo.SpaceVO;
import jakarta.servlet.http.HttpServletRequest;

public interface SpaceService extends IService<Space> {

    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    void validSpace(Space space, boolean isAdd);

    void fillSpaceBySpaceLevel(Space space);

    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    void checkSpaceCapacity(Space space, long addSize);
}
