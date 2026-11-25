package com.sxzp.module.stats.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用「名称 + 数量」统计项，用于类型/学院/认证状态等分布图。
 */
@Data
public class NameCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 名称（岗位类型代码、学院名称、认证状态代码等） */
    private String name;

    /** 数量 */
    private Long value;
}
