import request from './request'

export function listByType(typeCode) {
  return request({
    url: `/dict/${typeCode}`,
    method: 'get',
  })
}
