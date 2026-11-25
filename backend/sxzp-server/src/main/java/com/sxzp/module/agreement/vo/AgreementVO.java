package com.sxzp.module.agreement.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实习协议展示对象。
 */
@Data
public class AgreementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long agreementId;

    private Long applyId;

    private Long jobId;

    private String jobTitle;

    private Long studentId;

    /** 学生姓名（企业视角） */
    private String studentName;

    private String studentNo;

    private String college;

    private Long companyId;

    private String companyName;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal salary;

    private String content;

    /** pending/signed/rejected */
    private String state;

    private LocalDateTime signTime;

    private LocalDateTime createTime;
}
