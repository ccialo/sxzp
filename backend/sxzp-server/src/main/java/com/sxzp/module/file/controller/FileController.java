package com.sxzp.module.file.controller;

import com.sxzp.common.exception.BizException;
import com.sxzp.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传接口：企业资质、简历附件等。
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

    /** 允许上传的文件类型 */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "zip", "rar");

    @Value("${sxzp.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * 上传文件，返回可访问的相对路径（如 /uploads/2026-09-14/xxx.pdf）。
     *
     * @param file 上传的文件
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new BizException("文件名无效");
        }
        // 校验扩展名
        String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BizException("不支持的文件类型：" + ext);
        }
        // 按日期分目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        File dir = new File(uploadDir, datePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BizException("创建上传目录失败");
        }
        // 生成唯一文件名
        String newName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File dest = new File(dir, newName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件保存失败: {}", e.getMessage());
            throw new BizException("文件保存失败");
        }
        // 返回可访问的相对路径（配合 WebMvcConfig 的资源映射）
        String relativePath = "/uploads/" + datePath + "/" + newName;
        log.info("文件上传成功: {}", relativePath);
        return Result.success("上传成功", relativePath);
    }
}
