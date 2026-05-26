package com.darkyellowcat.catvaultbackend.model.vo;

import lombok.Data;

import java.util.List;

/*
 * 图片标签和分类列表
 */
@Data
public class PictureTagCategory {

    /*
     * 标签列表
     */
    private List<String> tagList;

    /*
     * 分类列表
     */
    private List<String> categoryList;


}
