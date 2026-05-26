package com.darkyellowcat.catvaultbackend.model.vo;

import com.darkyellowcat.catvaultbackend.model.entity.Picture;
import com.darkyellowcat.catvaultbackend.model.entity.Space;
import com.darkyellowcat.catvaultbackend.model.entity.SpaceUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VOConversionTest {

    // === PictureVO ===

    @Test
    @DisplayName("PictureVO.objToVo 应正确转换基本字段")
    void testPictureObjToVo() {
        Picture picture = new Picture();
        picture.setId(1L);
        picture.setUrl("https://example.com/img.webp");
        picture.setName("test");
        picture.setPicWidth(800);
        picture.setPicHeight(600);
        picture.setPicColor("#ff0000");
        picture.setTags("[\"tag1\",\"tag2\"]");
        picture.setUserId(100L);
        picture.setSpaceId(200L);

        PictureVO vo = PictureVO.objToVo(picture);

        assertNotNull(vo);
        assertEquals(1L, vo.getId());
        assertEquals("https://example.com/img.webp", vo.getUrl());
        assertEquals("test", vo.getName());
        assertEquals(800, vo.getPicWidth());
        assertEquals(600, vo.getPicHeight());
        assertEquals("#ff0000", vo.getPicColor());
        assertEquals(100L, vo.getUserId());
        assertEquals(200L, vo.getSpaceId());
        assertNotNull(vo.getTags());
        assertEquals(2, vo.getTags().size());
        assertEquals("tag1", vo.getTags().get(0));
    }

    @Test
    @DisplayName("PictureVO.objToVo 对 null 应返回 null")
    void testPictureObjToVoNull() {
        assertNull(PictureVO.objToVo(null));
    }

    @Test
    @DisplayName("PictureVO.voToObj 应正确转换回实体")
    void testPictureVoToObj() {
        PictureVO vo = new PictureVO();
        vo.setId(1L);
        vo.setUrl("https://example.com/img.webp");
        vo.setName("test");
        vo.setTags(Arrays.asList("tag1", "tag2"));

        Picture picture = PictureVO.voToObj(vo);

        assertNotNull(picture);
        assertEquals(1L, picture.getId());
        assertEquals("test", picture.getName());
        assertTrue(picture.getTags().contains("tag1"));
    }

    @Test
    @DisplayName("PictureVO.voToObj 对 null 应返回 null")
    void testPictureVoToObjNull() {
        assertNull(PictureVO.voToObj(null));
    }

    @Test
    @DisplayName("PictureVO tags 为 null 时 objToVo 不应抛异常")
    void testPictureObjToVoNullTags() {
        Picture picture = new Picture();
        picture.setId(1L);
        picture.setTags(null);
        PictureVO vo = PictureVO.objToVo(picture);
        assertNotNull(vo);
    }

    // === SpaceVO ===

    @Test
    @DisplayName("SpaceVO.objToVo 应正确转换")
    void testSpaceObjToVo() {
        Space space = new Space();
        space.setId(1L);
        space.setSpaceName("测试空间");
        space.setSpaceLevel(1);
        space.setSpaceType(1);
        space.setMaxSize(1024L);
        space.setMaxCount(100L);
        space.setTotalSize(512L);
        space.setTotalCount(50L);
        space.setUserId(10L);

        SpaceVO vo = SpaceVO.objToVo(space);

        assertNotNull(vo);
        assertEquals(1L, vo.getId());
        assertEquals("测试空间", vo.getSpaceName());
        assertEquals(1, vo.getSpaceLevel());
        assertEquals(1, vo.getSpaceType());
        assertEquals(1024L, vo.getMaxSize());
        assertEquals(100L, vo.getMaxCount());
        assertEquals(512L, vo.getTotalSize());
        assertEquals(50L, vo.getTotalCount());
        assertEquals(10L, vo.getUserId());
    }

    @Test
    @DisplayName("SpaceVO.objToVo 对 null 应返回 null")
    void testSpaceObjToVoNull() {
        assertNull(SpaceVO.objToVo(null));
    }

    // === SpaceUserVO ===

    @Test
    @DisplayName("SpaceUserVO.objToVo 应正确转换")
    void testSpaceUserObjToVo() {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setId(1L);
        spaceUser.setSpaceId(10L);
        spaceUser.setUserId(20L);
        spaceUser.setSpaceRole("editor");
        spaceUser.setCreateTime(new Date());

        SpaceUserVO vo = SpaceUserVO.objToVo(spaceUser);

        assertNotNull(vo);
        assertEquals(1L, vo.getId());
        assertEquals(10L, vo.getSpaceId());
        assertEquals(20L, vo.getUserId());
        assertEquals("editor", vo.getSpaceRole());
        assertNotNull(vo.getCreateTime());
    }

    @Test
    @DisplayName("SpaceUserVO.objToVo 对 null 应返回 null")
    void testSpaceUserObjToVoNull() {
        assertNull(SpaceUserVO.objToVo(null));
    }
}
