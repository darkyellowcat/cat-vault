package com.darkyellowcat.catvaultbackend.model.dto.space;

import com.darkyellowcat.catvaultbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private String spaceName;

    private Integer spaceLevel;

    private Integer spaceType;

    private Long userId;

    private static final long serialVersionUID = 1L;
}
