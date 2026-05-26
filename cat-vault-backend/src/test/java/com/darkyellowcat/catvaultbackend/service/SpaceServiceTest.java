package com.darkyellowcat.catvaultbackend.service;

import com.darkyellowcat.catvaultbackend.exception.BusinessException;
import com.darkyellowcat.catvaultbackend.model.dto.space.SpaceQueryRequest;
import com.darkyellowcat.catvaultbackend.model.entity.Space;
import com.darkyellowcat.catvaultbackend.model.enums.SpaceLevelEnum;
import com.darkyellowcat.catvaultbackend.model.enums.SpaceTypeEnum;
import com.darkyellowcat.catvaultbackend.service.impl.SpaceServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class SpaceServiceTest {

    @Autowired
    private SpaceService spaceService;

    @Test
    @DisplayName("fillSpaceBySpaceLevel 应正确填充免费版配额")
    void testFillSpaceFreeLevel() {
        Space space = new Space();
        space.setSpaceLevel(SpaceLevelEnum.FREE.getValue());
        spaceService.fillSpaceBySpaceLevel(space);
        assertEquals(SpaceLevelEnum.FREE.getMaxCount(), space.getMaxCount());
        assertEquals(SpaceLevelEnum.FREE.getMaxSize(), space.getMaxSize());
        assertEquals(0L, space.getTotalSize());
        assertEquals(0L, space.getTotalCount());
    }

    @Test
    @DisplayName("fillSpaceBySpaceLevel 应正确填充旗舰版配额")
    void testFillSpaceFlagshipLevel() {
        Space space = new Space();
        space.setSpaceLevel(SpaceLevelEnum.FLAGSHIP.getValue());
        spaceService.fillSpaceBySpaceLevel(space);
        assertEquals(SpaceLevelEnum.FLAGSHIP.getMaxCount(), space.getMaxCount());
        assertEquals(SpaceLevelEnum.FLAGSHIP.getMaxSize(), space.getMaxSize());
    }

    @Test
    @DisplayName("validSpace 空间名为空时应抛出异常")
    void testValidSpaceEmptyName() {
        Space space = new Space();
        space.setSpaceLevel(0);
        assertThrows(BusinessException.class, () ->
                spaceService.validSpace(space, true));
    }

    @Test
    @DisplayName("validSpace 空间名过长应抛出异常")
    void testValidSpaceLongName() {
        Space space = new Space();
        space.setSpaceName("a".repeat(31));
        space.setSpaceLevel(0);
        assertThrows(BusinessException.class, () ->
                spaceService.validSpace(space, false));
    }

    @Test
    @DisplayName("validSpace 非法空间级别应抛出异常")
    void testValidSpaceInvalidLevel() {
        Space space = new Space();
        space.setSpaceName("test");
        space.setSpaceLevel(99);
        assertThrows(BusinessException.class, () ->
                spaceService.validSpace(space, true));
    }

    @Test
    @DisplayName("checkSpaceCapacity 超出数量限制应抛出异常")
    void testCheckCapacityCountExceeded() {
        Space space = new Space();
        space.setMaxCount(100L);
        space.setMaxSize(1024L * 1024 * 1024);
        space.setTotalCount(100L);
        space.setTotalSize(0L);
        assertThrows(BusinessException.class, () ->
                spaceService.checkSpaceCapacity(space, 1024));
    }

    @Test
    @DisplayName("checkSpaceCapacity 超出容量限制应抛出异常")
    void testCheckCapacitySizeExceeded() {
        Space space = new Space();
        space.setMaxCount(1000L);
        space.setMaxSize(100L);
        space.setTotalCount(0L);
        space.setTotalSize(90L);
        assertThrows(BusinessException.class, () ->
                spaceService.checkSpaceCapacity(space, 20));
    }

    @Test
    @DisplayName("checkSpaceCapacity 未超限不应抛出异常")
    void testCheckCapacityWithinLimit() {
        Space space = new Space();
        space.setMaxCount(100L);
        space.setMaxSize(1024L * 1024);
        space.setTotalCount(50L);
        space.setTotalSize(512L * 1024);
        assertDoesNotThrow(() -> spaceService.checkSpaceCapacity(space, 1024));
    }

    @Test
    @DisplayName("fillSpaceBySpaceLevel 专业版配额应正确")
    void testFillSpaceProLevel() {
        Space space = new Space();
        space.setSpaceLevel(SpaceLevelEnum.PRO.getValue());
        spaceService.fillSpaceBySpaceLevel(space);
        assertEquals(SpaceLevelEnum.PRO.getMaxCount(), space.getMaxCount());
        assertEquals(SpaceLevelEnum.PRO.getMaxSize(), space.getMaxSize());
    }

    @Test
    @DisplayName("fillSpaceBySpaceLevel 已有自定义配额不应被覆盖")
    void testFillSpaceCustomQuota() {
        Space space = new Space();
        space.setSpaceLevel(SpaceLevelEnum.FREE.getValue());
        space.setMaxSize(999L);
        space.setMaxCount(999L);
        spaceService.fillSpaceBySpaceLevel(space);
        assertEquals(999L, space.getMaxSize());
        assertEquals(999L, space.getMaxCount());
    }

    @Test
    @DisplayName("validSpace 非法空间类型应抛出异常")
    void testValidSpaceInvalidType() {
        Space space = new Space();
        space.setSpaceName("test");
        space.setSpaceLevel(0);
        space.setSpaceType(99);
        assertThrows(BusinessException.class, () ->
                spaceService.validSpace(space, true));
    }

    @Test
    @DisplayName("validSpace 合法参数不应抛出异常")
    void testValidSpaceValid() {
        Space space = new Space();
        space.setSpaceName("我的空间");
        space.setSpaceLevel(0);
        space.setSpaceType(SpaceTypeEnum.TEAM.getValue());
        assertDoesNotThrow(() -> spaceService.validSpace(space, true));
    }

    @Test
    @DisplayName("validSpace 更新时空间名为空不应抛出异常")
    void testValidSpaceUpdateEmptyName() {
        Space space = new Space();
        space.setSpaceLevel(0);
        assertDoesNotThrow(() -> spaceService.validSpace(space, false));
    }

    @Test
    @DisplayName("getQueryWrapper 对 null 应返回空 wrapper")
    void testGetQueryWrapperNull() {
        QueryWrapper<Space> wrapper = spaceService.getQueryWrapper(null);
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("getQueryWrapper 应正确构建查询条件")
    void testGetQueryWrapper() {
        SpaceQueryRequest request = new SpaceQueryRequest();
        request.setSpaceName("测试");
        request.setSpaceLevel(1);
        request.setUserId(100L);
        QueryWrapper<Space> wrapper = spaceService.getQueryWrapper(request);
        assertNotNull(wrapper);
        String sql = wrapper.getTargetSql();
        assertTrue(sql.contains("spaceName"));
        assertTrue(sql.contains("spaceLevel"));
        assertTrue(sql.contains("userId"));
    }

    @Test
    @DisplayName("getQueryWrapper 非法排序字段应抛出异常")
    void testGetQueryWrapperInvalidSort() {
        SpaceQueryRequest request = new SpaceQueryRequest();
        request.setSortField("DROP TABLE space;--");
        assertThrows(BusinessException.class, () -> spaceService.getQueryWrapper(request));
    }

    @Test
    @DisplayName("getQueryWrapper 合法排序字段应正常")
    void testGetQueryWrapperValidSort() {
        SpaceQueryRequest request = new SpaceQueryRequest();
        request.setSortField("totalCount");
        request.setSortOrder("descend");
        QueryWrapper<Space> wrapper = spaceService.getQueryWrapper(request);
        assertNotNull(wrapper);
    }
}
