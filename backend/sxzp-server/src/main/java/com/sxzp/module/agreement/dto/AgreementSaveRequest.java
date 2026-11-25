package com.sxzp.module.agreement.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 企业发起实习协议参数。
 */
@Data
public class AgreementSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关联投递记录ID */
    @NotNull(message = "投递记录ID不能为空")
    private Long applyId;

    /** 实习开始日期 */
    private LocalDate startDate;

    /** 实习结束日期 */
    private LocalDate endDate;

    /** 实习津贴 */
    private BigDecimal salary;

    /** 协议内容 */
    private String content;
}
