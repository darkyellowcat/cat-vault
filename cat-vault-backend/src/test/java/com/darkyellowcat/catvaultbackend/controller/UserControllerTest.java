package com.darkyellowcat.catvaultbackend.controller;

import com.darkyellowcat.catvaultbackend.model.dto.user.UserRegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("注册接口 - 参数为空应返回错误")
    void testRegisterEmpty() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("注册接口 - 账号过短应返回错误")
    void testRegisterShortAccount() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUserAccount("ab");
        request.setUserPassword("12345678");
        request.setCheckPassword("12345678");
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("登录接口 - 参数为空应返回错误")
    void testLoginEmpty() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("登录接口 - 不存在的用户应返回错误")
    void testLoginNonExistUser() throws Exception {
        String body = "{\"userAccount\":\"nonexist_user_xyz\",\"userPassword\":\"12345678\"}";
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("获取当前用户 - 未登录应返回未登录错误")
    void testGetLoginUserNotLoggedIn() throws Exception {
        mockMvc.perform(post("/user/get/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    @DisplayName("获取用户VO - id 非法应返回错误")
    void testGetUserVOInvalidId() throws Exception {
        mockMvc.perform(get("/user/get/vo").param("id", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }
}
