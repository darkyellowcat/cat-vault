package com.darkyellowcat.catvaultbackend.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumTest {

    // === UserRoleEnum ===

    @Test
    @DisplayName("UserRoleEnum.getEnumByValue 应正确返回 USER")
    void testUserRoleUser() {
        assertEquals(UserRoleEnum.USER, UserRoleEnum.getEnumByValue("user"));
    }

    @Test
    @DisplayName("UserRoleEnum.getEnumByValue 应正确返回 ADMIN")
    void testUserRoleAdmin() {
        assertEquals(UserRoleEnum.ADMIN, UserRoleEnum.getEnumByValue("admin"));
    }

    @Test
    @DisplayName("UserRoleEnum.getEnumByValue 对 null 应返回 null")
    void testUserRoleNull() {
        assertNull(UserRoleEnum.getEnumByValue(null));
    }

    @Test
    @DisplayName("UserRoleEnum.getEnumByValue 对非法值应返回 null")
    void testUserRoleInvalid() {
        assertNull(UserRoleEnum.getEnumByValue("superadmin"));
    }

    // === SpaceLevelEnum ===

    @Test
    @DisplayName("SpaceLevelEnum.getEnumByValue 应正确返回各级别")
    void testSpaceLevelValues() {
        assertEquals(SpaceLevelEnum.FREE, SpaceLevelEnum.getEnumByValue(0));
        assertEquals(SpaceLevelEnum.PRO, SpaceLevelEnum.getEnumByValue(1));
        assertEquals(SpaceLevelEnum.FLAGSHIP, SpaceLevelEnum.getEnumByValue(2));
    }

    @Test
    @DisplayName("SpaceLevelEnum.getEnumByValue 对 null 应返回 null")
    void testSpaceLevelNull() {
        assertNull(SpaceLevelEnum.getEnumByValue(null));
    }

    @Test
    @DisplayName("SpaceLevelEnum.getEnumByValue 对非法值应返回 null")
    void testSpaceLevelInvalid() {
        assertNull(SpaceLevelEnum.getEnumByValue(99));
    }

    @Test
    @DisplayName("SpaceLevelEnum 配额值应合理")
    void testSpaceLevelQuotas() {
        assertEquals(100L, SpaceLevelEnum.FREE.getMaxCount());
        assertEquals(100L * 1024 * 1024, SpaceLevelEnum.FREE.getMaxSize());
        assertEquals(1000L, SpaceLevelEnum.PRO.getMaxCount());
        assertEquals(10000L, SpaceLevelEnum.FLAGSHIP.getMaxCount());
        assertTrue(SpaceLevelEnum.PRO.getMaxSize() > SpaceLevelEnum.FREE.getMaxSize());
        assertTrue(SpaceLevelEnum.FLAGSHIP.getMaxSize() > SpaceLevelEnum.PRO.getMaxSize());
    }

    // === SpaceRoleEnum ===

    @Test
    @DisplayName("SpaceRoleEnum.getEnumByValue 应正确返回各角色")
    void testSpaceRoleValues() {
        assertEquals(SpaceRoleEnum.VIEWER, SpaceRoleEnum.getEnumByValue("viewer"));
        assertEquals(SpaceRoleEnum.EDITOR, SpaceRoleEnum.getEnumByValue("editor"));
        assertEquals(SpaceRoleEnum.ADMIN, SpaceRoleEnum.getEnumByValue("admin"));
    }

    @Test
    @DisplayName("SpaceRoleEnum.getEnumByValue 对非法值应返回 null")
    void testSpaceRoleInvalid() {
        assertNull(SpaceRoleEnum.getEnumByValue("owner"));
        assertNull(SpaceRoleEnum.getEnumByValue(null));
    }

    // === SpaceTypeEnum ===

    @Test
    @DisplayName("SpaceTypeEnum.getEnumByValue 应正确返回各类型")
    void testSpaceTypeValues() {
        assertEquals(SpaceTypeEnum.PRIVATE, SpaceTypeEnum.getEnumByValue(0));
        assertEquals(SpaceTypeEnum.TEAM, SpaceTypeEnum.getEnumByValue(1));
    }

    @Test
    @DisplayName("SpaceTypeEnum.getEnumByValue 对非法值应返回 null")
    void testSpaceTypeInvalid() {
        assertNull(SpaceTypeEnum.getEnumByValue(5));
        assertNull(SpaceTypeEnum.getEnumByValue(null));
    }

    // === PictureReviewStatusEnum ===

    @Test
    @DisplayName("PictureReviewStatusEnum.getEnumByValue 应正确返回各状态")
    void testPictureReviewStatusValues() {
        assertEquals(PictureReviewStatusEnum.REVIEWING, PictureReviewStatusEnum.getEnumByValue(0));
        assertEquals(PictureReviewStatusEnum.PASS, PictureReviewStatusEnum.getEnumByValue(1));
        assertEquals(PictureReviewStatusEnum.REJECT, PictureReviewStatusEnum.getEnumByValue(2));
    }

    @Test
    @DisplayName("PictureReviewStatusEnum.getEnumByValue 对非法值应返回 null")
    void testPictureReviewStatusInvalid() {
        assertNull(PictureReviewStatusEnum.getEnumByValue(null));
        assertNull(PictureReviewStatusEnum.getEnumByValue(99));
    }
}
