package com.sxzp.module.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 注册请求参数（学生/企业）。
 */
@Data
public class RegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 登录账号：学生用学号，企业用企业账号 */
    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 40, message = "账号长度需在 4-40 之间")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 之间")
    private String password;

    /** student 或 company */
    @NotBlank(message = "用户类型不能为空")
    private String userType;

    private String realName;

    private String email;

    private String phone;

    /** 学生：学号；企业：单位名称 */
    private String studentNo;

    private String college;

    private String major;

    private String gradeYear;

    /** 企业：单位名称 */
    private String entName;

    /** 企业：统一社会信用代码 */
    private String creditCode;

    private String category;

    private String address;
}
