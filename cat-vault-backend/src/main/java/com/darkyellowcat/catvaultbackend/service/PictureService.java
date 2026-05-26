package com.darkyellowcat.catvaultbackend.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureQueryRequest;
import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureReviewRequest;
import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureUploadByBatchRequest;
import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureUploadRequest;
import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureEditByBatchRequest;
import com.darkyellowcat.catvaultbackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.darkyellowcat.catvaultbackend.model.entity.User;
import com.darkyellowcat.catvaultbackend.model.vo.PictureVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author darkcarrot
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2026-02-23 21:59:32
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param object             上传源，可以是 MultipartFile 或 String（图片 URL）
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object object,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 构建查询条件
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片视图对象
     *
     * @param picture 图片实体
     * @param request
     * @return 图片视图对象
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片视图对象类
     *
     * @param picturePage 图片实体列表
     * @param request
     * @return 图片视图对象列表
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);


    /**
     * 校验图片是否合法
     *
     * @param picture 图片实体
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充图片审核参数
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    int uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

    /**
     * 删除图片文件
     *
     * @param oldPicture 旧图片实体
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 按颜色相似度搜索图片
     *
     * @param picColor 目标颜色（十六进制）
     * @param spaceId  空间 id（可选）
     * @param request
     * @return 按颜色相似度排序的图片列表
     */
    List<PictureVO> searchByColor(String picColor, Long spaceId, HttpServletRequest request);

    /**
     * 构建编辑后的图片 URL（基于 COS 数据万象参数）
     *
     * @param originalUrl 原始图片 URL
     * @param editRequest 编辑参数
     * @return 带处理参数的图片 URL
     */
    String buildEditedImageUrl(String originalUrl, PictureEditByBatchRequest editRequest);
}
