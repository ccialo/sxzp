package com.sxzp.module.apply.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 学生投递简历参数。
 */
@Data
public class ApplySubmitRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "岗位ID不能为空")
    private Long jobId;

    /** 指定投递的简历ID；为空时使用默认简历 */
    private Long resumeId;
}
