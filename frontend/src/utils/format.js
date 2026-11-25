import dayjs from 'dayjs'

const DATETIME_FMT = 'YYYY-MM-DD HH:mm:ss'
const DATE_FMT = 'YYYY-MM-DD'

/**
 * 格式化日期时间
 */
export function formatDateTime(value, fmt = DATETIME_FMT) {
  if (!value) return '-'
  return dayjs(value).format(fmt)
}

/**
 * 格式化日期
 */
export function formatDate(value) {
  if (!value) return '-'
  return dayjs(value).format(DATE_FMT)
}

/**
 * 格式化金额：保留两位小数
 */
export function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '-'
  const num = Number(value)
  if (Number.isNaN(num)) return '-'
  return '¥' + num.toFixed(2)
}

/**
 * 格式化手机号：脱敏中间 4 位
 */
export function formatPhone(phone) {
  if (!phone || phone.length < 11) return phone || '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 截断文本
 */
export function truncate(text, max = 50) {
  if (!text) return ''
  return text.length > max ? text.slice(0, max) + '...' : text
}
