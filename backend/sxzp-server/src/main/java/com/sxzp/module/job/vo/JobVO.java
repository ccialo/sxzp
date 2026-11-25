package com.sxzp.module.job.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 岗位展示对象（带企业信息与投递统计）。
 */
@Data
public class JobVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long jobId;

    private Long companyId;

    /** 企业名称 */
    private String companyName;

    /** 企业认证状态 */
    private String companyAuthState;

    private String jobTitle;

    private String jobType;

    private Integer headcount;

    private String workPlace;

    private String category;

    private String req;

    private String duty;

    private BigDecimal benefitMoney;

    private LocalDate startDate;

    private LocalDate endDate;

    private String auditState;

    private String auditOpinion;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    /** 已收到投递数（企业/管理员视角） */
    private Long applyCount;

    /** 当前登录学生是否已投递（学生视角） */
    private Boolean applied;
}
