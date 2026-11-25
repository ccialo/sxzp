package com.sxzp.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxzp.module.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * sys_user 数据访问。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
