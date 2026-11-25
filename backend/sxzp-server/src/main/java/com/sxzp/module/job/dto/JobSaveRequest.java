package com.sxzp.module.job.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 岗位新增/修改参数。
 */
@Data
public class JobSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 为空表示新增，不为空表示修改 */
    private Long jobId;

    @NotBlank(message = "岗位名称不能为空")
    private String jobTitle;

    /** 实习类型：teaching/professional/graduation */
    private String jobType;

    @Positive(message = "拟招人数必须大于 0")
    private Integer headcount;

    private String workPlace;

    private String category;

    /** 任职要求 */
    private String req;

    /** 岗位职责 */
    private String duty;

    /** 实习津贴 */
    private BigDecimal benefitMoney;

    private LocalDate startDate;

    private LocalDate endDate;

    /** 是否直接提交审核：true=提交审核，false=存为草稿 */
    private Boolean submit = Boolean.FALSE;
}
