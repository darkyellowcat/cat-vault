package com.darkyellowcat.catvaultbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "message")
@Data
public class Message implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private Integer hasRead;

    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
