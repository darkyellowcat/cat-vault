package com.darkyellowcat.catvaultbackend.event;

import com.darkyellowcat.catvaultbackend.model.entity.Message;
import com.darkyellowcat.catvaultbackend.model.enums.PictureReviewStatusEnum;
import com.darkyellowcat.catvaultbackend.mapper.MessageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PictureReviewEventListener {

    @Resource
    private MessageMapper messageMapper;

    @Async
    @EventListener
    public void onPictureReview(PictureReviewEvent event) {
        Message message = new Message();
        message.setUserId(event.getUserId());
        message.setHasRead(0);

        PictureReviewStatusEnum statusEnum = PictureReviewStatusEnum.getEnumByValue(event.getReviewStatus());
        if (PictureReviewStatusEnum.PASS.equals(statusEnum)) {
            message.setTitle("图片审核通过");
            message.setContent("您的图片已通过审核，现在可以被其他用户看到了。");
        } else if (PictureReviewStatusEnum.REJECT.equals(statusEnum)) {
            message.setTitle("图片审核未通过");
            String reason = event.getReviewMessage() != null ? event.getReviewMessage() : "未说明原因";
            message.setContent("您的图片未通过审核，原因：" + reason);
        } else {
            return;
        }

        messageMapper.insert(message);
        log.info("通知已发送: userId={}, title={}", event.getUserId(), message.getTitle());
    }
}
