package com.sxzp.module.dict.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据字典取值 dict_value。
 */
@Data
@TableName("dict_value")
public class DictValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "dict_id", type = IdType.AUTO)
    private Long dictId;

    private String typeCode;

    private String dictCode;

    private String dictLabel;

    private Integer sort;

    private Integer isEnabled;
}
