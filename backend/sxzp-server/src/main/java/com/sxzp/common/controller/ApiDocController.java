package com.sxzp.common.controller;

import com.sxzp.common.config.ApiDocScanner;
import com.sxzp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口文档端点：扫描所有 Controller 自动生成 API 清单。
 * 访问 /api/doc 获取 JSON 格式接口列表。
 */
@RestController
@RequestMapping("/api/doc")
@RequiredArgsConstructor
public class ApiDocController {

    private final ApiDocScanner apiDocScanner;

    /** 返回全部接口清单（按模块分组） */
    @GetMapping
    public Result<Map<String, Object>> doc() {
        Map<String, Object> result = new HashMap<>();
        result.put("modules", apiDocScanner.getModules());
        result.put("total", apiDocScanner.getEntries().size());

        // 按模块分组
        Map<String, List<ApiDocScanner.ApiDocEntry>> grouped = apiDocScanner.getEntries().stream()
                .collect(Collectors.groupingBy(ApiDocScanner.ApiDocEntry::getModule));
        result.put("endpoints", grouped);
        return Result.success(result);
    }
}
