package com.sxzp.module.company.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 企业认证审核参数（管理员）。
 */
@Data
public class CompanyAuditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "企业ID不能为空")
    private Long companyId;

    /** 是否通过 */
    @NotNull(message = "审核结论不能为空")
    private Boolean pass;

    /** 驳回意见 */
    private String opinion;
}
