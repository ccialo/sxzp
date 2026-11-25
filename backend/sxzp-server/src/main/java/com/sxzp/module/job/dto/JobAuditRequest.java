package com.sxzp.module.job.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 岗位审核参数（管理员）。
 */
@Data
public class JobAuditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "岗位ID不能为空")
    private Long jobId;

    @NotNull(message = "审核结论不能为空")
    private Boolean pass;

    /** 驳回意见 */
    private String opinion;
}
