package com.darkyellowcat.catvaultbackend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PictureReviewEvent extends ApplicationEvent {

    private final Long pictureId;
    private final Long userId;
    private final Integer reviewStatus;
    private final String reviewMessage;

    public PictureReviewEvent(Object source, Long pictureId, Long userId, Integer reviewStatus, String reviewMessage) {
        super(source);
        this.pictureId = pictureId;
        this.userId = userId;
        this.reviewStatus = reviewStatus;
        this.reviewMessage = reviewMessage;
    }
}
