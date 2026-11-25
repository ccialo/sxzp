package com.sxzp.module.dict.controller;

import com.sxzp.common.result.Result;
import com.sxzp.module.dict.entity.DictValue;
import com.sxzp.module.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据字典接口：前端下拉框/枚举取值。
 */
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    /**
     * 按类型编码查询字典取值列表。
     *
     * @param typeCode 字典类型，如 job_type / apply_state / company_category
     */
    @GetMapping("/{typeCode}")
    public Result<List<DictValue>> list(@PathVariable String typeCode) {
        return Result.success(dictService.listByType(typeCode));
    }
}
