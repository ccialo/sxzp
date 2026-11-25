/**
 * 通用状态映射表：state -> { label, type }
 * type 用于 el-tag 颜色：primary/success/warning/danger/info
 */

// 投递状态（与后端 job_apply.apply_state 取值保持一致，passed 为历史兼容项）
export const APPLY_STATE_MAP = {
  pending: { label: '待处理', type: 'info' },
  interview: { label: '进入面试', type: 'warning' },
  rejected: { label: '未通过', type: 'danger' },
  passed: { label: '通过', type: 'success' },
  agreement: { label: '待协议', type: 'primary' },
  hired: { label: '已录用', type: 'success' },
  withdrawn: { label: '已撤回', type: 'info' },
}

// 面试状态
export const INTERVIEW_STATE_MAP = {
  pending: { label: '待确认', type: 'info' },
  confirmed: { label: '已确认', type: 'warning' },
  cancelled: { label: '已取消', type: 'danger' },
  done: { label: '已完成', type: 'success' },
}

// 协议状态
export const AGREEMENT_STATE_MAP = {
  pending: { label: '待签署', type: 'info' },
  signed: { label: '已签署', type: 'success' },
  rejected: { label: '已拒绝', type: 'danger' },
}

// 周报状态
export const WEEKLY_STATE_MAP = {
  submitted: { label: '待评价', type: 'info' },
  reviewed: { label: '已评价', type: 'success' },
}

// 企业认证状态
export const COMPANY_AUTH_STATE_MAP = {
  pending: { label: '待审核', type: 'warning' },
  auth: { label: '已认证', type: 'success' },
  reject: { label: '未通过', type: 'danger' },
  freeze: { label: '已冻结', type: 'info' },
}

// 岗位审核状态
export const JOB_AUDIT_STATE_MAP = {
  draft: { label: '草稿', type: 'info' },
  pending: { label: '待审核', type: 'warning' },
  online: { label: '已上线', type: 'success' },
  reject: { label: '已驳回', type: 'danger' },
  ended: { label: '已下线', type: 'danger' },
}

// 消息类型
export const NOTICE_TYPE_MAP = {
  system: { label: '系统', type: 'info' },
  interview: { label: '面试', type: 'warning' },
  agreement: { label: '协议', type: 'success' },
  apply: { label: '投递', type: 'primary' },
}

/**
 * 根据 state + map 获取 label
 */
export function getLabel(map, state) {
  return map[state]?.label || state || '-'
}

/**
 * 根据 state + map 获取 el-tag type
 */
export function getTagType(map, state) {
  return map[state]?.type || 'info'
}
