package com.sxzp.module.resume.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sxzp.common.exception.BizException;
import com.sxzp.common.security.LoginUser;
import com.sxzp.common.security.LoginUserContext;
import com.sxzp.module.resume.dto.ResumeSaveRequest;
import com.sxzp.module.resume.entity.StudentResume;
import com.sxzp.module.resume.mapper.StudentResumeMapper;
import com.sxzp.module.student.entity.Student;
import com.sxzp.module.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 简历服务：学生维护自己的简历。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final StudentResumeMapper resumeMapper;
    private final StudentMapper studentMapper;

    /** 当前登录学生档案 */
    public Student currentStudent() {
        LoginUser loginUser = LoginUserContext.require();
        Student student = studentMapper.selectOne(
                Wrappers.<Student>lambdaQuery().eq(Student::getUserOwner, loginUser.getUserId()));
        if (student == null) {
            throw new BizException(404, "未找到学生档案");
        }
        return student;
    }

    /** 我的简历列表 */
    public List<StudentResume> listMine() {
        Student student = currentStudent();
        return resumeMapper.selectList(Wrappers.<StudentResume>lambdaQuery()
                .eq(StudentResume::getStudentId, student.getStudentId())
                .orderByDesc(StudentResume::getIsDefault)
                .orderByDesc(StudentResume::getUpdateTime));
    }

    /** 简历详情（本人） */
    public StudentResume detail(Long resumeId) {
        StudentResume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BizException(404, "简历不存在");
        }
        Student student = currentStudent();
        if (!student.getStudentId().equals(resume.getStudentId())) {
            throw new BizException(403, "无权查看他人简历");
        }
        return resume;
    }

    /** 我的默认简历（无则取最新一份） */
    public StudentResume defaultResume(Long studentId) {
        LambdaQueryWrapper<StudentResume> wrapper = Wrappers.<StudentResume>lambdaQuery()
                .eq(StudentResume::getStudentId, studentId)
                .orderByDesc(StudentResume::getIsDefault)
                .orderByDesc(StudentResume::getUpdateTime)
                .last("limit 1");
        return resumeMapper.selectOne(wrapper);
    }

    /** 新增/修改简历 */
    @Transactional(rollbackFor = Exception.class)
    public StudentResume save(ResumeSaveRequest request) {
        Student student = currentStudent();
        StudentResume resume;
        if (request.getResumeId() == null) {
            resume = new StudentResume();
            resume.setStudentId(student.getStudentId());
        } else {
            resume = resumeMapper.selectById(request.getResumeId());
            if (resume == null) {
                throw new BizException(404, "简历不存在");
            }
            if (!student.getStudentId().equals(resume.getStudentId())) {
                throw new BizException(403, "无权修改他人简历");
            }
        }
        BeanUtils.copyProperties(request, resume, "resumeId");
        if (resume.getIsDefault() != null && resume.getIsDefault() == 1) {
            // 默认简历唯一
            clearDefault(student.getStudentId());
        }
        if (resume.getResumeId() == null) {
            resumeMapper.insert(resume);
        } else {
            resumeMapper.updateById(resume);
        }
        log.info("简历保存: resumeId={}, studentId={}", resume.getResumeId(), student.getStudentId());
        return resume;
    }

    /** 设置默认投递简历 */
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long resumeId) {
        Student student = currentStudent();
        StudentResume resume = resumeMapper.selectById(resumeId);
        if (resume == null || !student.getStudentId().equals(resume.getStudentId())) {
            throw new BizException(404, "简历不存在");
        }
        clearDefault(student.getStudentId());
        resume.setIsDefault(1);
        resumeMapper.updateById(resume);
    }

    /** 删除简历 */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long resumeId) {
        Student student = currentStudent();
        StudentResume resume = resumeMapper.selectById(resumeId);
        if (resume == null || !student.getStudentId().equals(resume.getStudentId())) {
            throw new BizException(404, "简历不存在");
        }
        resumeMapper.deleteById(resumeId);
    }

    private void clearDefault(Long studentId) {
        StudentResume update = new StudentResume();
        update.setIsDefault(0);
        resumeMapper.update(update, Wrappers.<StudentResume>lambdaUpdate()
                .eq(StudentResume::getStudentId, studentId)
                .eq(StudentResume::getIsDefault, 1));
    }
}
