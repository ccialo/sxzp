package com.sxzp.module.weekly.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实习周报 weekly_report（含企业评价）。
 */
@Data
@TableName("weekly_report")
public class WeeklyReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    /** 关联投递记录 */
    private Long applyId;

    private Long jobId;

    private Long studentId;

    private Long companyId;

    /** 第几周 */
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

    /** 企业评分 1-5，NULL 表示未评 */
    private Integer reviewScore;

    /** 企业评语 */
    private String reviewComment;

    /** submitted(已提交)/reviewed(已评价) */
    private String state;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
