package com.sxzp.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色表 sys_role（RBAC）。
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    /** 角色代码 student/company/admin */
    private String roleCode;

    /** 角色显示名 */
    private String roleName;

    /** 逗号分隔的权限标识 */
    private String permissions;
}
