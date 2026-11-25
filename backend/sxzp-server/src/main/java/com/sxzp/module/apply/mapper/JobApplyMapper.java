package com.sxzp.module.apply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxzp.module.apply.entity.JobApply;
import org.apache.ibatis.annotations.Mapper;

/**
 * job_apply 数据访问。
 */
@Mapper
public interface JobApplyMapper extends BaseMapper<JobApply> {
}
