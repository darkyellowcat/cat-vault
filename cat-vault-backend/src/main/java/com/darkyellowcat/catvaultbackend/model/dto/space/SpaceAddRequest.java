package com.darkyellowcat.catvaultbackend.model.dto.space;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceAddRequest implements Serializable {

    @NotBlank(message = "空间名称不能为空")
    @Size(max = 30, message = "空间名称不能超过30个字符")
    private String spaceName;

    @NotNull(message = "空间级别不能为空")
    private Integer spaceLevel;

    private Integer spaceType;

    private static final long serialVersionUID = 1L;
}
