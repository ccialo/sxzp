package com.sxzp.module.job.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实习岗位 job。
 */
@Data
@TableName("job")
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "job_id", type = IdType.AUTO)
    private Long jobId;

    /** 所属企业 */
    private Long companyId;

    private String jobTitle;

    /** 实习类型（字典 job_type） */
    private String jobType;

    /** 拟招人数 */
    private Integer headcount;

    private String workPlace;

    /** 岗位方向 */
    private String category;

    /** 任职要求 */
    private String req;

    /** 岗位职责 */
    private String duty;

    /** 实习津贴 */
    private BigDecimal benefitMoney;

    private LocalDate startDate;

    private LocalDate endDate;

    /** draft/pending/online/reject/ended */
    private String auditState;

    private String auditOpinion;

    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
