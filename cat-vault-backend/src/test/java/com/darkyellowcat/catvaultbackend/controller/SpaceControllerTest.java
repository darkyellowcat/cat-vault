package com.darkyellowcat.catvaultbackend.controller;

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
class SpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("创建空间 - 未登录应返回未登录错误")
    void testAddSpaceNotLoggedIn() throws Exception {
        String body = "{\"spaceName\":\"test\",\"spaceLevel\":0}";
        mockMvc.perform(post("/space/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    @DisplayName("获取空间详情 - id 非法应返回错误")
    void testGetSpaceVOInvalidId() throws Exception {
        mockMvc.perform(get("/space/get/vo").param("id", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("分页查询空间 - 应正常返回")
    void testListSpaceVOByPage() throws Exception {
        String body = "{\"current\":1,\"pageSize\":10}";
        mockMvc.perform(post("/space/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("分页查询空间 - pageSize 超限应返回错误")
    void testListSpaceVOByPageOverLimit() throws Exception {
        String body = "{\"current\":1,\"pageSize\":100}";
        mockMvc.perform(post("/space/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("我的空间 - 未登录应返回未登录错误")
    void testMySpacesNotLoggedIn() throws Exception {
        mockMvc.perform(get("/space/my/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    @DisplayName("添加成员 - 未登录应返回未登录错误")
    void testAddMemberNotLoggedIn() throws Exception {
        String body = "{\"spaceId\":1,\"userId\":2}";
        mockMvc.perform(post("/space/member/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    @DisplayName("成员列表 - 缺少 spaceId 应返回参数错误")
    void testListMembersNoSpaceId() throws Exception {
        String body = "{}";
        mockMvc.perform(post("/space/member/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }
}
