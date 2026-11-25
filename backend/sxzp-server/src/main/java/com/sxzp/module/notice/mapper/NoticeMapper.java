package com.sxzp.module.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxzp.module.notice.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

/**
 * notice 数据访问。
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
