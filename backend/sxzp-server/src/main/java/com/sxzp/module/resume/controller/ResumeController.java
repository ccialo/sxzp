package com.sxzp.module.resume.controller;

import com.sxzp.common.result.Result;
import com.sxzp.module.resume.dto.ResumeSaveRequest;
import com.sxzp.module.resume.entity.StudentResume;
import com.sxzp.module.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生端 - 简历管理接口。
 */
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class ResumeController {

    private final ResumeService resumeService;

    /** 我的简历列表 */
    @GetMapping("/list")
    public Result<List<StudentResume>> list() {
        return Result.success(resumeService.listMine());
    }

    /** 简历详情 */
    @GetMapping("/detail/{resumeId}")
    public Result<StudentResume> detail(@PathVariable Long resumeId) {
        return Result.success(resumeService.detail(resumeId));
    }

    /** 新增/修改简历 */
    @PostMapping("/save")
    public Result<StudentResume> save(@Validated @RequestBody ResumeSaveRequest request) {
        return Result.success("保存成功", resumeService.save(request));
    }

    /** 设为默认投递简历 */
    @PutMapping("/default/{resumeId}")
    public Result<Void> setDefault(@PathVariable Long resumeId) {
        resumeService.setDefault(resumeId);
        return Result.success("已设为默认投递简历", null);
    }

    /** 删除简历 */
    @DeleteMapping("/{resumeId}")
    public Result<Void> delete(@PathVariable Long resumeId) {
        resumeService.delete(resumeId);
        return Result.success("删除成功", null);
    }
}
