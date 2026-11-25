package com.sxzp.module.stats.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 投递趋势统计项（按日）。
 */
@Data
public class ApplyTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期 */
    private LocalDate date;

    /** 当日投递数 */
    private Long count;
}
