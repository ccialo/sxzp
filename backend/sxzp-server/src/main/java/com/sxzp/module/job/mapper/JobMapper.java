package com.sxzp.module.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxzp.module.job.entity.Job;
import org.apache.ibatis.annotations.Mapper;

/**
 * job 数据访问。
 */
@Mapper
public interface JobMapper extends BaseMapper<Job> {
}
