package com.sxzp.module.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxzp.module.student.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * student 数据访问。
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
