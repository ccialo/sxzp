import request from './request'

/** 我的简历列表 */
export function listResumes() {
  return request({
    url: '/resume/list',
    method: 'get',
  })
}

/** 简历详情 */
export function resumeDetail(resumeId) {
  return request({
    url: `/resume/detail/${resumeId}`,
    method: 'get',
  })
}

/** 新增/修改简历 */
export function saveResume(data) {
  return request({
    url: '/resume/save',
    method: 'post',
    data,
  })
}

/** 设为默认投递简历 */
export function setDefault(resumeId) {
  return request({
    url: `/resume/default/${resumeId}`,
    method: 'put',
  })
}

/** 删除简历 */
export function deleteResume(resumeId) {
  return request({
    url: `/resume/${resumeId}`,
    method: 'delete',
  })
}
