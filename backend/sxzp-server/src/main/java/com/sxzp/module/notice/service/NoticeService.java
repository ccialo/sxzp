package com.sxzp.module.notice.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxzp.common.exception.BizException;
import com.sxzp.common.result.PageResult;
import com.sxzp.common.security.LoginUserContext;
import com.sxzp.module.notice.entity.Notice;
import com.sxzp.module.notice.mapper.NoticeMapper;
import com.sxzp.module.notice.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息通知服务：站内信的发送、查询、已读标记。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;

    /** 通知类型常量 */
    public static final String TYPE_SYSTEM = "system";
    public static final String TYPE_INTERVIEW = "interview";
    public static final String TYPE_AGREEMENT = "agreement";
    public static final String TYPE_APPLY = "apply";

    /**
     * 发送一条站内通知（供其他业务模块调用）。
     *
     * @param userId  接收用户ID
     * @param title   标题
     * @param content 内容
     * @param type    通知类型
     */
    public void send(Long userId, String title, String content, String type) {
        if (userId == null) {
            return;
        }
        Notice notice = new Notice();
        notice.setUserId(userId);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setType(type);
        notice.setIsRead(0);
        noticeMapper.insert(notice);
        log.debug("发送通知: userId={}, title={}", userId, title);
    }

    /** 我的消息列表（未读优先，按时间倒序） */
    public PageResult<NoticeVO> pageMine(int current, int size, String type) {
        Long userId = LoginUserContext.requireUserId();
        Page<Notice> page = noticeMapper.selectPage(new Page<>(current, size),
                Wrappers.<Notice>lambdaQuery()
                        .eq(Notice::getUserId, userId)
                        .eq(type != null && !type.isEmpty(), Notice::getType, type)
                        .orderByAsc(Notice::getIsRead)
                        .orderByDesc(Notice::getCreateTime));
        List<NoticeVO> list = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 未读消息数 */
    public long countUnread() {
        Long userId = LoginUserContext.requireUserId();
        Long count = noticeMapper.selectCount(Wrappers.<Notice>lambdaQuery()
                .eq(Notice::getUserId, userId)
                .eq(Notice::getIsRead, 0));
        return count == null ? 0L : count;
    }

    /** 标记单条已读 */
    @Transactional(rollbackFor = Exception.class)
    public void read(Long noticeId) {
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BizException(404, "通知不存在");
        }
        Long userId = LoginUserContext.requireUserId();
        if (!userId.equals(notice.getUserId())) {
            throw new BizException(403, "无权操作他人通知");
        }
        if (notice.getIsRead() != null && notice.getIsRead() == 1) {
            return;
        }
        notice.setIsRead(1);
        noticeMapper.updateById(notice);
    }

    /** 标记全部已读 */
    @Transactional(rollbackFor = Exception.class)
    public void readAll() {
        Long userId = LoginUserContext.requireUserId();
        Notice update = new Notice();
        update.setIsRead(1);
        noticeMapper.update(update, Wrappers.<Notice>lambdaUpdate()
                .eq(Notice::getUserId, userId)
                .eq(Notice::getIsRead, 0));
    }

    private NoticeVO toVO(Notice notice) {
        NoticeVO vo = new NoticeVO();
        BeanUtils.copyProperties(notice, vo);
        return vo;
    }
}
