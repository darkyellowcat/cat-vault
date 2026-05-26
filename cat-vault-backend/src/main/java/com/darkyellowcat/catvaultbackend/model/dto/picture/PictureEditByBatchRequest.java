package com.darkyellowcat.catvaultbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureEditByBatchRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 裁剪区域 x 坐标
     */
    private Integer cropX;

    /**
     * 裁剪区域 y 坐标
     */
    private Integer cropY;

    /**
     * 裁剪宽度
     */
    private Integer cropWidth;

    /**
     * 裁剪高度
     */
    private Integer cropHeight;

    /**
     * 旋转角度（0/90/180/270）
     */
    private Integer rotate;

    /**
     * 缩放宽度
     */
    private Integer scaleWidth;

    /**
     * 缩放高度
     */
    private Integer scaleHeight;

    /**
     * 水印文字
     */
    private String watermarkText;

    /**
     * 输出格式（jpg/png/webp）
     */
    private String format;

    private static final long serialVersionUID = 1L;
}
