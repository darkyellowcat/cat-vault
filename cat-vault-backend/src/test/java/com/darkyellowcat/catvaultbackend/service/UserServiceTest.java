package com.darkyellowcat.catvaultbackend.service;

import com.darkyellowcat.catvaultbackend.exception.BusinessException;
import com.darkyellowcat.catvaultbackend.model.dto.user.UserQueryRequest;
import com.darkyellowcat.catvaultbackend.model.entity.User;
import com.darkyellowcat.catvaultbackend.model.vo.LoginUserVO;
import com.darkyellowcat.catvaultbackend.service.impl.UserServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("密码加密应返回非空且不等于原文")
    void testGetEncryptPassword() {
        String raw = "testPassword123";
        String encrypted = userService.getEncryptPassword(raw);
        assertNotNull(encrypted);
        assertNotEquals(raw, encrypted);
    }

    @Test
    @DisplayName("相同密码两次加密结果应不同（BCrypt 特性）")
    void testBCryptRandomSalt() {
        String raw = "testPassword123";
        String enc1 = userService.getEncryptPassword(raw);
        String enc2 = userService.getEncryptPassword(raw);
        assertNotEquals(enc1, enc2);
    }

    @Test
    @DisplayName("注册时账号过短应抛出异常")
    void testRegisterShortAccount() {
        assertThrows(BusinessException.class, () ->
                userService.userRegister("ab", "12345678", "12345678"));
    }

    @Test
    @DisplayName("注册时密码过短应抛出异常")
    void testRegisterShortPassword() {
        assertThrows(BusinessException.class, () ->
                userService.userRegister("testuser", "123", "123"));
    }

    @Test
    @DisplayName("注册时两次密码不一致应抛出异常")
    void testRegisterPasswordMismatch() {
        assertThrows(BusinessException.class, () ->
                userService.userRegister("testuser", "12345678", "87654321"));
    }

    @Test
    @DisplayName("isAdmin 对普通用户应返回 false")
    void testIsAdminForNormalUser() {
        User user = new User();
        user.setUserRole("user");
        assertFalse(userService.isAdmin(user));
    }

    @Test
    @DisplayName("isAdmin 对管理员应返回 true")
    void testIsAdminForAdmin() {
        User user = new User();
        user.setUserRole("admin");
        assertTrue(userService.isAdmin(user));
    }

    @Test
    @DisplayName("getUserVO 对 null 应返回 null")
    void testGetUserVONull() {
        assertNull(userService.getUserVO(null));
    }

    @Test
    @DisplayName("getUserVO 应正确脱敏")
    void testGetUserVO() {
        User user = new User();
        user.setId(1L);
        user.setUserAccount("testuser");
        user.setUserPassword("encrypted");
        user.setUserName("Test");
        user.setUserRole("user");
        var vo = userService.getUserVO(user);
        assertNotNull(vo);
        assertEquals("testuser", vo.getUserAccount());
        assertEquals("Test", vo.getUserName());
    }

    @Test
    @DisplayName("getLoginUserVO 应正确转换")
    void testGetLoginUserVO() {
        User user = new User();
        user.setId(1L);
        user.setUserAccount("testuser");
        user.setUserName("Test");
        user.setUserRole("admin");
        LoginUserVO loginUserVO = userService.getLoginUserVO(user);
        assertNotNull(loginUserVO);
        assertEquals(1L, loginUserVO.getId());
        assertEquals("testuser", loginUserVO.getUserAccount());
    }

    @Test
    @DisplayName("getUserVOList 空列表应返回空列表")
    void testGetUserVOListEmpty() {
        List<User> emptyList = new ArrayList<>();
        List<?> result = userService.getUserVOList(emptyList);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getUserVOList null 应返回空列表")
    void testGetUserVOListNull() {
        List<?> result = userService.getUserVOList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getUserVOList 应正确批量转换")
    void testGetUserVOListMultiple() {
        User u1 = new User();
        u1.setId(1L);
        u1.setUserAccount("user1");
        User u2 = new User();
        u2.setId(2L);
        u2.setUserAccount("user2");
        var result = userService.getUserVOList(Arrays.asList(u1, u2));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("isAdmin 对 null 应返回 false")
    void testIsAdminNull() {
        assertFalse(userService.isAdmin(null));
    }

    @Test
    @DisplayName("getQueryWrapper 对 null 应抛出异常")
    void testGetQueryWrapperNull() {
        assertThrows(BusinessException.class, () -> userService.getQueryWrapper(null));
    }

    @Test
    @DisplayName("getQueryWrapper 应正确构建查询条件")
    void testGetQueryWrapper() {
        UserQueryRequest request = new UserQueryRequest();
        request.setUserName("test");
        request.setUserRole("admin");
        QueryWrapper<User> wrapper = userService.getQueryWrapper(request);
        assertNotNull(wrapper);
        String sql = wrapper.getTargetSql();
        assertTrue(sql.contains("userName"));
        assertTrue(sql.contains("userRole"));
    }

    @Test
    @DisplayName("getQueryWrapper 非法排序字段应抛出异常")
    void testGetQueryWrapperInvalidSort() {
        UserQueryRequest request = new UserQueryRequest();
        request.setSortField("DROP TABLE user; --");
        assertThrows(BusinessException.class, () -> userService.getQueryWrapper(request));
    }

    @Test
    @DisplayName("getQueryWrapper 合法排序字段应正常")
    void testGetQueryWrapperValidSort() {
        UserQueryRequest request = new UserQueryRequest();
        request.setSortField("createTime");
        request.setSortOrder("ascend");
        QueryWrapper<User> wrapper = userService.getQueryWrapper(request);
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("注册时参数为 null 应抛出异常")
    void testRegisterNullParams() {
        assertThrows(BusinessException.class, () ->
                userService.userRegister(null, "12345678", "12345678"));
        assertThrows(BusinessException.class, () ->
                userService.userRegister("testuser", null, "12345678"));
    }
}
