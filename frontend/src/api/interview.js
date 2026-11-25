import request from './request'

/** 企业：安排面试 */
export function scheduleInterview(data) {
  return request({
    url: '/interview/schedule',
    method: 'post',
    data,
  })
}

/** 学生：我的面试列表 */
export function myInterviews(params) {
  return request({
    url: '/interview/my',
    method: 'get',
    params,
  })
}

/** 企业：我发出的面试列表 */
export function sentInterviews(params) {
  return request({
    url: '/interview/sent',
    method: 'get',
    params,
  })
}

/** 学生：确认面试 */
export function confirmInterview(interviewId) {
  return request({
    url: `/interview/confirm/${interviewId}`,
    method: 'put',
  })
}

/** 取消面试（学生/企业均可） */
export function cancelInterview(interviewId) {
  return request({
    url: `/interview/cancel/${interviewId}`,
    method: 'put',
  })
}

/** 企业：标记面试完成 */
export function doneInterview(interviewId) {
  return request({
    url: `/interview/done/${interviewId}`,
    method: 'put',
  })
}
