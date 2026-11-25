package com.sxzp.module.apply.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 企业处理投递（筛选）参数。
 */
@Data
public class ApplyAuditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "投递ID不能为空")
    private Long applyId;

    /** 目标状态：interview(进入面试) / rejected(淘汰) */
    @NotBlank(message = "目标状态不能为空")
    private String applyState;
}
