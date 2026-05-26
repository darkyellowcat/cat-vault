package com.darkyellowcat.catvaultbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darkyellowcat.catvaultbackend.exception.BusinessException;
import com.darkyellowcat.catvaultbackend.exception.ErrorCode;
import com.darkyellowcat.catvaultbackend.exception.ThrowUtils;
import com.darkyellowcat.catvaultbackend.config.CosClientConfig;
import com.darkyellowcat.catvaultbackend.manager.CosManager;
import com.darkyellowcat.catvaultbackend.manager.FileManager;
import com.darkyellowcat.catvaultbackend.manager.upload.FilePictureUpload;
import com.darkyellowcat.catvaultbackend.manager.upload.PictureUploadTemplate;
import com.darkyellowcat.catvaultbackend.manager.upload.UrlPictureUpload;
import com.darkyellowcat.catvaultbackend.model.dto.file.UploadPictureResult;
import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureEditByBatchRequest;
import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureQueryRequest;
import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureReviewRequest;
import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureUploadByBatchRequest;
import com.darkyellowcat.catvaultbackend.model.dto.picture.PictureUploadRequest;
import com.darkyellowcat.catvaultbackend.model.entity.Picture;
import com.darkyellowcat.catvaultbackend.model.entity.Message;
import com.darkyellowcat.catvaultbackend.model.entity.Space;
import com.darkyellowcat.catvaultbackend.model.entity.SpaceUser;
import com.darkyellowcat.catvaultbackend.model.entity.User;
import com.darkyellowcat.catvaultbackend.model.enums.PictureReviewStatusEnum;
import com.darkyellowcat.catvaultbackend.model.vo.PictureVO;
import com.darkyellowcat.catvaultbackend.model.vo.UserVO;
import com.darkyellowcat.catvaultbackend.service.PictureService;
import com.darkyellowcat.catvaultbackend.mapper.MessageMapper;
import com.darkyellowcat.catvaultbackend.mapper.PictureMapper;
import com.darkyellowcat.catvaultbackend.service.SpaceService;
import com.darkyellowcat.catvaultbackend.service.SpaceUserService;
import com.darkyellowcat.catvaultbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author darkcarrot
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2026-02-23 21:59:32
*/
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private org.springframework.context.ApplicationEventPublisher applicationEventPublisher;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 用于判断是新增还是更新图片
        Long pictureId = null;
        Long spaceId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
            spaceId = pictureUploadRequest.getSpaceId();
        }
        // 如果指定了空间，校验空间存在且用户有编辑权限
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 校验用户是空间成员且有编辑权限（editor 或 admin）
            if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                QueryWrapper<SpaceUser> spaceUserQuery = new QueryWrapper<>();
                spaceUserQuery.eq("spaceId", spaceId).eq("userId", loginUser.getId());
                SpaceUser spaceUser = spaceUserService.getOne(spaceUserQuery);
                ThrowUtils.throwIf(spaceUser == null, ErrorCode.NO_AUTH_ERROR, "非空间成员");
                ThrowUtils.throwIf("viewer".equals(spaceUser.getSpaceRole()), ErrorCode.NO_AUTH_ERROR, "无编辑权限");
            }
            spaceService.checkSpaceCapacity(space, 0);
        } else {
            // 上传到公共图库需要管理员权限
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "仅管理员可上传到公共图库");
        }
        // 如果是更新图片，需要校验图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可编辑
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        // 上传图片，得到信息
        // 按照用户 id 划分目录 → 空间图片放到 space 目录
        String uploadPathPrefix;
        if (spaceId != null) {
            uploadPathPrefix = String.format("space/%s/%s", spaceId, loginUser.getId());
        } else {
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        }

        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        // 优先使用用户填写的名称，否则用文件名
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picture.setName(pictureUploadRequest.getPicName());
        } else {
            picture.setName(uploadPictureResult.getPicName());
        }
        // 用户填写的简介、分类、标签
        if (pictureUploadRequest != null) {
            if (StrUtil.isNotBlank(pictureUploadRequest.getIntroduction())) {
                picture.setIntroduction(pictureUploadRequest.getIntroduction());
            }
            if (StrUtil.isNotBlank(pictureUploadRequest.getCategory())) {
                picture.setCategory(pictureUploadRequest.getCategory());
            }
            if (StrUtil.isNotBlank(pictureUploadRequest.getTags())) {
                picture.setTags(pictureUploadRequest.getTags());
            }
        }
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setPicColor(uploadPictureResult.getPicColor());
        picture.setFileHash(uploadPictureResult.getFileHash());
        picture.setUserId(loginUser.getId());
        picture.setSpaceId(spaceId);
        // 填充审核信息：空间内图片自动过审，公共图库走审核流程
        if (spaceId != null) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("空间图片自动过审");
            picture.setReviewTime(new Date());
        } else {
            this.fillReviewParams(picture, loginUser);
        }
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        // 更新空间容量（仅新增图片时）
        if (spaceId != null && pictureId == null) {
            boolean updated = spaceService.lambdaUpdate()
                    .eq(Space::getId, spaceId)
                    .setSql("totalSize = totalSize + " + picture.getPicSize())
                    .setSql("totalCount = totalCount + 1")
                    .update();
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新空间容量失败");
            // 通知上传者
            Message msg = new Message();
            msg.setUserId(loginUser.getId());
            msg.setTitle("图片上传成功");
            msg.setContent("您的图片「" + picture.getName() + "」已上传到空间");
            msg.setHasRead(0);
            messageMapper.insert(msg);
        }
        return PictureVO.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        String picColor = pictureQueryRequest.getPicColor();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();

        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        // 公共图库查询时排除空间内的图片
        if (spaceId == null && ObjUtil.isNotEmpty(reviewStatus)) {
            queryWrapper.isNull("spaceId");
        }
        queryWrapper.eq(StrUtil.isNotBlank(picColor), "picColor", picColor);

        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序（白名单校验防止 SQL 注入）
        if (StrUtil.isNotEmpty(sortField)) {
            List<String> validSortFields = Arrays.asList("id", "name", "picSize", "picWidth", "picHeight",
                    "picScale", "createTime", "editTime", "updateTime", "reviewStatus", "spaceId");
            ThrowUtils.throwIf(!validSortFields.contains(sortField), ErrorCode.PARAMS_ERROR, "非法排序字段");
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), sortField);
        }
        return queryWrapper;
    }


    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 已是该状态
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
        // 更新审核状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 发布审核事件通知用户
        applicationEventPublisher.publishEvent(
                new com.darkyellowcat.catvaultbackend.event.PictureReviewEvent(
                        this, id, oldPicture.getUserId(), reviewStatus, pictureReviewRequest.getReviewMessage()));
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，创建或编辑都要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public int uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
        // 格式化数量
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多 30 条");
        // 要抓取的地址
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl)
                    .timeout(10000)
                    .get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
//        Elements imgElementList = div.select("img.mimg");
        Elements imgElementList = div.select(".iusc");  // 修改选择器，获取包含完整数据的元素
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            //String fileUrl = imgElement.attr("src");

            // 获取data-m属性中的JSON字符串
            String dataM = imgElement.attr("m");
            String fileUrl;
            try {
                // 解析JSON字符串
                JSONObject jsonObject = JSONUtil.parseObj(dataM);
                // 获取murl字段（原始图片URL）
                fileUrl = jsonObject.getStr("murl");
            } catch (Exception e) {
                log.error("解析图片数据失败", e);
                continue;
            }
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断该图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery()
                .eq(Picture::getUrl, pictureUrl)
                .count();
        // 有不止一条记录用到了该图片，不清理
        if (count > 1) {
            return;
        }
        cosManager.deleteObject(extractKeyFromUrl(pictureUrl));
        // 清理缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(extractKeyFromUrl(thumbnailUrl));
        }
    }

    private String extractKeyFromUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return url;
        }
        // 去除域名前缀，提取 COS key
        String host = cosClientConfig.getHost();
        if (url.startsWith(host)) {
            return url.substring(host.length());
        }
        return url;
    }

    @Override
    public List<PictureVO> searchByColor(String picColor, Long spaceId, HttpServletRequest request) {
        List<Picture> pictures = this.lambdaQuery()
                .isNotNull(Picture::getPicColor)
                .eq(spaceId != null, Picture::getSpaceId, spaceId)
                .eq(spaceId == null, Picture::getReviewStatus, PictureReviewStatusEnum.PASS.getValue())
                .last("LIMIT 1000")
                .list();
        // 解析目标颜色
        int[] targetRgb = hexToRgb(picColor);
        // 按颜色距离排序
        return pictures.stream()
                .sorted((a, b) -> {
                    int[] rgbA = hexToRgb(a.getPicColor());
                    int[] rgbB = hexToRgb(b.getPicColor());
                    double distA = colorDistance(targetRgb, rgbA);
                    double distB = colorDistance(targetRgb, rgbB);
                    return Double.compare(distA, distB);
                })
                .limit(20)
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
    }

    private int[] hexToRgb(String hex) {
        if (hex == null) {
            return new int[]{0, 0, 0};
        }
        // 支持 0xRRGGBB 和 #RRGGBB 格式
        hex = hex.replace("#", "").replace("0x", "");
        if (hex.length() < 6) {
            return new int[]{0, 0, 0};
        }
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    private double colorDistance(int[] c1, int[] c2) {
        int dr = c1[0] - c2[0];
        int dg = c1[1] - c2[1];
        int db = c1[2] - c2[2];
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    @Override
    public String buildEditedImageUrl(String originalUrl, PictureEditByBatchRequest editRequest) {
        ThrowUtils.throwIf(StrUtil.isBlank(originalUrl), ErrorCode.PARAMS_ERROR, "图片 URL 不能为空");
        StringBuilder params = new StringBuilder();
        // 裁剪
        if (editRequest.getCropWidth() != null && editRequest.getCropHeight() != null) {
            int x = editRequest.getCropX() != null ? editRequest.getCropX() : 0;
            int y = editRequest.getCropY() != null ? editRequest.getCropY() : 0;
            params.append("imageMogr2/cut/")
                    .append(editRequest.getCropWidth()).append("x").append(editRequest.getCropHeight())
                    .append("x").append(x).append("x").append(y);
        }
        // 缩放
        if (editRequest.getScaleWidth() != null || editRequest.getScaleHeight() != null) {
            if (params.length() > 0) params.append("|");
            params.append("imageMogr2/thumbnail/");
            if (editRequest.getScaleWidth() != null && editRequest.getScaleHeight() != null) {
                params.append(editRequest.getScaleWidth()).append("x").append(editRequest.getScaleHeight()).append("!");
            } else if (editRequest.getScaleWidth() != null) {
                params.append(editRequest.getScaleWidth()).append("x");
            } else {
                params.append("x").append(editRequest.getScaleHeight());
            }
        }
        // 旋转
        if (editRequest.getRotate() != null && editRequest.getRotate() != 0) {
            if (params.length() > 0) params.append("|");
            params.append("imageMogr2/rotate/").append(editRequest.getRotate());
        }
        // 格式转换
        if (StrUtil.isNotBlank(editRequest.getFormat())) {
            if (params.length() > 0) params.append("|");
            params.append("imageMogr2/format/").append(editRequest.getFormat());
        }
        // 文字水印
        if (StrUtil.isNotBlank(editRequest.getWatermarkText())) {
            if (params.length() > 0) params.append("|");
            String encodedText = cn.hutool.core.codec.Base64.encode(editRequest.getWatermarkText());
            params.append("watermark/2/text/").append(encodedText)
                    .append("/fontsize/20/fill/IzAwMDAwMA");
        }
        if (params.length() == 0) {
            return originalUrl;
        }
        String separator = originalUrl.contains("?") ? "&" : "?";
        return originalUrl + separator + params;
    }

}




