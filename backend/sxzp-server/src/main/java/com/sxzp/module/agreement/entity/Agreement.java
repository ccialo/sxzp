package com.sxzp.module.agreement.entity;

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
 * 实习协议 agreement。
 */
@Data
@TableName("agreement")
public class Agreement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "agreement_id", type = IdType.AUTO)
    private Long agreementId;

    /** 关联投递记录 */
    private Long applyId;

    private Long jobId;

    private Long studentId;

    private Long companyId;

    /** 实习开始日期 */
    private LocalDate startDate;

    /** 实习结束日期 */
    private LocalDate endDate;

    /** 实习津贴 */
    private BigDecimal salary;

    /** 协议内容 */
    private String content;

    /** pending(待签署)/signed(已签署)/rejected(已拒绝) */
    private String state;

    /** 签署时间 */
    private LocalDateTime signTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
