package com.darkyellowcat.catvaultbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户添加请求
 *
 * @author darkcarrot
 */
@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = 1786911623741909980L;
    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;


}

