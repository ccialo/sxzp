import request from './request'

/** 我的消息列表 */
export function listNotices(params) {
  return request({
    url: '/notice/list',
    method: 'get',
    params,
  })
}

/** 未读消息数 */
export function unreadCount() {
  return request({
    url: '/notice/unread-count',
    method: 'get',
  })
}

/** 标记单条已读 */
export function readNotice(noticeId) {
  return request({
    url: `/notice/read/${noticeId}`,
    method: 'put',
  })
}

/** 标记全部已读 */
export function readAllNotices() {
  return request({
    url: '/notice/read-all',
    method: 'put',
  })
}
