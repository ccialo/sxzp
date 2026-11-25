import { ElMessage } from 'element-plus'

export function success(msg) {
  ElMessage.success(msg)
}

export function error(msg) {
  ElMessage.error(msg)
}

export function warning(msg) {
  ElMessage.warning(msg)
}

export function info(msg) {
  ElMessage.info(msg)
}

export default { success, error, warning, info }
