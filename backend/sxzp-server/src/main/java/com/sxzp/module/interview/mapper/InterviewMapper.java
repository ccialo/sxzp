package com.sxzp.module.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxzp.module.interview.entity.Interview;
import org.apache.ibatis.annotations.Mapper;

/**
 * interview 数据访问。
 */
@Mapper
public interface InterviewMapper extends BaseMapper<Interview> {
}
