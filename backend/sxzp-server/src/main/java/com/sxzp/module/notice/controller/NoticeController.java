package com.sxzp.module.notice.controller;

import com.sxzp.common.result.PageResult;
import com.sxzp.common.result.Result;
import com.sxzp.module.notice.service.NoticeService;
import com.sxzp.module.notice.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息通知接口：站内信查询与已读标记。
 */
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /** 我的消息列表（未读优先） */
    @GetMapping("/list")
    public Result<PageResult<NoticeVO>> list(@RequestParam(defaultValue = "1") int current,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String type) {
        return Result.success(noticeService.pageMine(current, size, type));
    }

    /** 未读消息数 */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(noticeService.countUnread());
    }

    /** 标记单条已读 */
    @PutMapping("/read/{noticeId}")
    public Result<Void> read(@PathVariable Long noticeId) {
        noticeService.read(noticeId);
        return Result.success("已标记已读", null);
    }

    /** 标记全部已读 */
    @PutMapping("/read-all")
    public Result<Void> readAll() {
        noticeService.readAll();
        return Result.success("全部已读", null);
    }
}
