package com.sxzp.module.company.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxzp.module.company.entity.Company;
import org.apache.ibatis.annotations.Mapper;

/**
 * company 数据访问。
 */
@Mapper
public interface CompanyMapper extends BaseMapper<Company> {
}
