package com.darkyellowcat.catvaultbackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUserQueryRequest implements Serializable {

    private Long id;

    private Long spaceId;

    private Long userId;

    private String spaceRole;

    private static final long serialVersionUID = 1L;
}
