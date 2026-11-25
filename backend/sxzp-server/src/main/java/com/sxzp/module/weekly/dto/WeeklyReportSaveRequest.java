package com.sxzp.module.weekly.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 学生提交实习周报参数。
 */
@Data
public class WeeklyReportSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关联投递记录ID */
    @NotNull(message = "投递记录ID不能为空")
    private Long applyId;

    /** 第几周 */
    @NotNull(message = "周次不能为空")
    private Integer weekNum;

    /** 本周开始日期 */
    private LocalDate startDate;

    /** 本周结束日期 */
    private LocalDate endDate;

    /** 本周工作内容 */
    private String content;

    /** 下周计划 */
    private String plan;

    /** 遇到的问题 */
    private String problem;
}
