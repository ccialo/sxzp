package com.sxzp.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 当前登录用户信息（由 JWT 解析得到）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 登录账号 */
    private String account;

    /** 用户类型：student/company/admin */
    private String userType;

    public boolean isAdmin() {
        return "admin".equals(userType);
    }

    public boolean isStudent() {
        return "student".equals(userType);
    }

    public boolean isCompany() {
        return "company".equals(userType);
    }
}
