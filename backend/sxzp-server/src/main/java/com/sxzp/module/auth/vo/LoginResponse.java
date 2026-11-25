package com.sxzp.module.auth.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录成功返回信息。
 */
@Data
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private Long userId;

    private String account;

    private String userType;

    private String realName;

    /** 关联业务ID：学生为 studentId，企业为 companyId，管理员为 null */
    private Long profileId;
}
