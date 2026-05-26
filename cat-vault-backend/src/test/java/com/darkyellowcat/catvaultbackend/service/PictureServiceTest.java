package com.darkyellowcat.catvaultbackend.service;

import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureEditByBatchRequest;
import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureQueryRequest;
import com.darkyellowcat.catvaultbackend.model.entity.Picture;
import com.darkyellowcat.catvaultbackend.model.entity.User;
import com.darkyellowcat.catvaultbackend.model.enums.PictureReviewStatusEnum;
import com.darkyellowcat.catvaultbackend.model.vo.PictureVO;
import com.darkyellowcat.catvaultbackend.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class PictureServiceTest {

    @Autowired
    private PictureService pictureService;

    @Test
    @DisplayName("validPicture 对 null 应抛出异常")
    void testValidPictureNull() {
        assertThrows(Exception.class, () -> pictureService.validPicture(null));
    }

    @Test
    @DisplayName("validPicture 对 id 为空应抛出异常")
    void testValidPictureNoId() {
        Picture picture = new Picture();
        assertThrows(Exception.class, () -> pictureService.validPicture(picture));
    }

    @Test
    @DisplayName("validPicture URL 过长应抛出异常")
    void testValidPictureLongUrl() {
        Picture picture = new Picture();
        picture.setId(1L);
        picture.setUrl("x".repeat(1025));
        assertThrows(Exception.class, () -> pictureService.validPicture(picture));
    }

    @Test
    @DisplayName("buildEditedImageUrl 裁剪参数应正确拼接")
    void testBuildEditedUrlCrop() {
        PictureEditByBatchRequest req = new PictureEditByBatchRequest();
        req.setCropX(10);
        req.setCropY(20);
        req.setCropWidth(200);
        req.setCropHeight(300);
        String url = pictureService.buildEditedImageUrl("https://example.com/img.webp", req);
        assertTrue(url.contains("imageMogr2/cut/200x300x10x20"));
    }

    @Test
    @DisplayName("buildEditedImageUrl 旋转参数应正确拼接")
    void testBuildEditedUrlRotate() {
        PictureEditByBatchRequest req = new PictureEditByBatchRequest();
        req.setRotate(90);
        String url = pictureService.buildEditedImageUrl("https://example.com/img.webp", req);
        assertTrue(url.contains("imageMogr2/rotate/90"));
    }

    @Test
    @DisplayName("buildEditedImageUrl 缩放参数应正确拼接")
    void testBuildEditedUrlScale() {
        PictureEditByBatchRequest req = new PictureEditByBatchRequest();
        req.setScaleWidth(800);
        req.setScaleHeight(600);
        String url = pictureService.buildEditedImageUrl("https://example.com/img.webp", req);
        assertTrue(url.contains("imageMogr2/thumbnail/800x600!"));
    }

    @Test
    @DisplayName("buildEditedImageUrl 格式转换应正确拼接")
    void testBuildEditedUrlFormat() {
        PictureEditByBatchRequest req = new PictureEditByBatchRequest();
        req.setFormat("png");
        String url = pictureService.buildEditedImageUrl("https://example.com/img.webp", req);
        assertTrue(url.contains("imageMogr2/format/png"));
    }

    @Test
    @DisplayName("buildEditedImageUrl 无参数应返回原 URL")
    void testBuildEditedUrlNoParams() {
        PictureEditByBatchRequest req = new PictureEditByBatchRequest();
        String url = pictureService.buildEditedImageUrl("https://example.com/img.webp", req);
        assertEquals("https://example.com/img.webp", url);
    }

    @Test
    @DisplayName("buildEditedImageUrl 多参数组合应用管道符分隔")
    void testBuildEditedUrlCombined() {
        PictureEditByBatchRequest req = new PictureEditByBatchRequest();
        req.setRotate(90);
        req.setFormat("png");
        String url = pictureService.buildEditedImageUrl("https://example.com/img.webp", req);
        assertTrue(url.contains("imageMogr2/rotate/90"));
        assertTrue(url.contains("imageMogr2/format/png"));
        assertTrue(url.contains("|"));
    }

    @Test
    @DisplayName("buildEditedImageUrl 仅宽度缩放")
    void testBuildEditedUrlScaleWidthOnly() {
        PictureEditByBatchRequest req = new PictureEditByBatchRequest();
        req.setScaleWidth(500);
        String url = pictureService.buildEditedImageUrl("https://example.com/img.webp", req);
        assertTrue(url.contains("imageMogr2/thumbnail/500x"));
    }

    @Test
    @DisplayName("buildEditedImageUrl 仅高度缩放")
    void testBuildEditedUrlScaleHeightOnly() {
        PictureEditByBatchRequest req = new PictureEditByBatchRequest();
        req.setScaleHeight(400);
        String url = pictureService.buildEditedImageUrl("https://example.com/img.webp", req);
        assertTrue(url.contains("imageMogr2/thumbnail/x400"));
    }

    @Test
    @DisplayName("buildEditedImageUrl 水印应包含 Base64 编码")
    void testBuildEditedUrlWatermark() {
        PictureEditByBatchRequest req = new PictureEditByBatchRequest();
        req.setWatermarkText("CatVault");
        String url = pictureService.buildEditedImageUrl("https://example.com/img.webp", req);
        assertTrue(url.contains("watermark/2/text/"));
    }

    @Test
    @DisplayName("buildEditedImageUrl 对空 URL 应抛出异常")
    void testBuildEditedUrlEmptyUrl() {
        PictureEditByBatchRequest req = new PictureEditByBatchRequest();
        req.setRotate(90);
        assertThrows(Exception.class, () -> pictureService.buildEditedImageUrl("", req));
        assertThrows(Exception.class, () -> pictureService.buildEditedImageUrl(null, req));
    }

    @Test
    @DisplayName("buildEditedImageUrl 裁剪默认坐标为 0")
    void testBuildEditedUrlCropDefaultXY() {
        PictureEditByBatchRequest req = new PictureEditByBatchRequest();
        req.setCropWidth(100);
        req.setCropHeight(100);
        String url = pictureService.buildEditedImageUrl("https://example.com/img.webp", req);
        assertTrue(url.contains("imageMogr2/cut/100x100x0x0"));
    }

    @Test
    @DisplayName("validPicture 简介过长应抛出异常")
    void testValidPictureLongIntroduction() {
        Picture picture = new Picture();
        picture.setId(1L);
        picture.setIntroduction("x".repeat(801));
        assertThrows(Exception.class, () -> pictureService.validPicture(picture));
    }

    @Test
    @DisplayName("validPicture 正常数据不应抛出异常")
    void testValidPictureNormal() {
        Picture picture = new Picture();
        picture.setId(1L);
        picture.setUrl("https://example.com/img.webp");
        picture.setIntroduction("一张测试图片");
        assertDoesNotThrow(() -> pictureService.validPicture(picture));
    }

    @Test
    @DisplayName("getQueryWrapper 对 null 应返回空 wrapper")
    void testGetQueryWrapperNull() {
        QueryWrapper<Picture> wrapper = pictureService.getQueryWrapper(null);
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("getQueryWrapper 应正确处理搜索文本")
    void testGetQueryWrapperSearchText() {
        PictureQueryRequest request = new PictureQueryRequest();
        request.setSearchText("猫");
        QueryWrapper<Picture> wrapper = pictureService.getQueryWrapper(request);
        assertNotNull(wrapper);
        String sql = wrapper.getTargetSql();
        assertTrue(sql.contains("name"));
    }

    @Test
    @DisplayName("getQueryWrapper 应正确处理标签查询")
    void testGetQueryWrapperTags() {
        PictureQueryRequest request = new PictureQueryRequest();
        request.setTags(Arrays.asList("搞笑", "高清"));
        QueryWrapper<Picture> wrapper = pictureService.getQueryWrapper(request);
        assertNotNull(wrapper);
        String sql = wrapper.getTargetSql();
        assertTrue(sql.contains("tags"));
    }

    @Test
    @DisplayName("getQueryWrapper 非法排序字段应抛出异常")
    void testGetQueryWrapperInvalidSort() {
        PictureQueryRequest request = new PictureQueryRequest();
        request.setSortField("1=1; DROP TABLE picture;--");
        assertThrows(BusinessException.class, () -> pictureService.getQueryWrapper(request));
    }

    @Test
    @DisplayName("getQueryWrapper 合法排序字段应正常")
    void testGetQueryWrapperValidSort() {
        PictureQueryRequest request = new PictureQueryRequest();
        request.setSortField("createTime");
        request.setSortOrder("descend");
        QueryWrapper<Picture> wrapper = pictureService.getQueryWrapper(request);
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("fillReviewParams 管理员应自动过审")
    void testFillReviewParamsAdmin() {
        Picture picture = new Picture();
        User admin = new User();
        admin.setId(1L);
        admin.setUserRole("admin");
        pictureService.fillReviewParams(picture, admin);
        assertEquals(PictureReviewStatusEnum.PASS.getValue(), picture.getReviewStatus());
        assertEquals(1L, picture.getReviewerId());
        assertNotNull(picture.getReviewTime());
    }

    @Test
    @DisplayName("fillReviewParams 普通用户应为待审核")
    void testFillReviewParamsUser() {
        Picture picture = new Picture();
        User user = new User();
        user.setId(2L);
        user.setUserRole("user");
        pictureService.fillReviewParams(picture, user);
        assertEquals(PictureReviewStatusEnum.REVIEWING.getValue(), picture.getReviewStatus());
    }
}
