package com.sxzp.module.weekly.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 企业评价周报参数。
 */
@Data
public class WeeklyReviewRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "周报ID不能为空")
    private Long reportId;

    /** 评分 1-5 */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer score;

    /** 评语 */
    private String comment;
}
