package com.sxzp.module.notice.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知展示对象。
 */
@Data
public class NoticeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long noticeId;

    private Long userId;

    private String title;

    private String content;

    /** system/interview/agreement/apply */
    private String type;

    /** 0 未读，1 已读 */
    private Integer isRead;

    private LocalDateTime createTime;
}
