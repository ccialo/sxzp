package com.sxzp.module.company.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业档案与认证 company。
 */
@Data
@TableName("company")
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "company_id", type = IdType.AUTO)
    private Long companyId;

    /** 绑定 sys_user.user_id */
    private Long userOwner;

    /** 单位名称 */
    private String entName;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 单位性质（字典 company_category） */
    private String category;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String address;

    private String intro;

    /** 资质附件路径 */
    private String certFile;

    /** pending/auth/reject/freeze */
    private String authState;

    /** 驳回意见 */
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
