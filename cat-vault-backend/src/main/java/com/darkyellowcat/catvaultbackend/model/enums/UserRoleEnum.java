package com.darkyellowcat.catvaultbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {
    /**
     * 普通用户
     */
    USER("普通用户", "user"),

    /**
     * 管理员
     */
    ADMIN("管理员", "admin");

    // 显示文本
    private final String text;

    // 存储值
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据存储值获取枚举实例
     *
     * @param value 存储值
     * @return 对应的枚举实例，如果未找到则返回 null
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum role : UserRoleEnum.values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        return null;
    }
}
