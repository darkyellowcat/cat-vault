package com.darkyellowcat.catvaultbackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUpdateRequest implements Serializable {

    private Long id;

    private String spaceName;

    private Integer spaceLevel;

    private static final long serialVersionUID = 1L;
}
