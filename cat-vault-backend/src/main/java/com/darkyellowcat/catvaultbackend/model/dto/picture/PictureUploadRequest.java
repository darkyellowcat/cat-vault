package com.darkyellowcat.catvaultbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片 id（用于修改）
     */
    private Long id;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组字符串）
     */
    private String tags;

    /**
     * 空间 id（null 表示公共图库）
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}

