package com.darkyellowcat.catvaultbackend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class PictureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("获取标签分类 - 应返回成功")
    void testGetTagCategory() throws Exception {
        mockMvc.perform(get("/picture/tag_category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.tagList").isArray())
                .andExpect(jsonPath("$.data.categoryList").isArray());
    }

    @Test
    @DisplayName("获取图片详情 - id 非法应返回错误")
    void testGetPictureVOInvalidId() throws Exception {
        mockMvc.perform(get("/picture/get/vo").param("id", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("颜色搜索 - 颜色参数过短应返回错误")
    void testSearchByColorInvalid() throws Exception {
        mockMvc.perform(get("/picture/search/color").param("picColor", "ab"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("分页查询 - 未登录也可访问公开图片列表")
    void testListPictureVOByPage() throws Exception {
        String body = "{\"current\":1,\"pageSize\":10}";
        mockMvc.perform(post("/picture/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("分页查询 - pageSize 超限应返回错误")
    void testListPictureVOByPageOverLimit() throws Exception {
        String body = "{\"current\":1,\"pageSize\":100}";
        mockMvc.perform(post("/picture/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @DisplayName("删除图片 - 未登录应返回未登录错误")
    void testDeletePictureNotLoggedIn() throws Exception {
        String body = "{\"id\":1}";
        mockMvc.perform(post("/picture/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    @DisplayName("上传图片 - 未登录应返回未登录错误")
    void testUploadPictureNotLoggedIn() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/picture/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));
    }
}
