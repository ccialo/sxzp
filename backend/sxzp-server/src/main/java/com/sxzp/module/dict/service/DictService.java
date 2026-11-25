package com.sxzp.module.dict.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sxzp.module.dict.entity.DictValue;
import com.sxzp.module.dict.mapper.DictValueMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据字典服务：按类型编码查询可用取值。
 */
@Service
@RequiredArgsConstructor
public class DictService {

    private final DictValueMapper dictValueMapper;

    /**
     * 按字典类型查询可用取值（按 sort 升序）。
     *
     * @param typeCode 字典类型编码，如 job_type / apply_state / company_category
     */
    public List<DictValue> listByType(String typeCode) {
        return dictValueMapper.selectList(Wrappers.<DictValue>lambdaQuery()
                .eq(DictValue::getTypeCode, typeCode)
                .eq(DictValue::getIsEnabled, 1)
                .orderByAsc(DictValue::getSort));
    }
}
