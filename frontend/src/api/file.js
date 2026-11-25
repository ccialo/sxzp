import request from './request'

/**
 * 上传文件
 * @param {File} file 文件对象
 * @param {(percent: number) => void} [onProgress] 进度回调
 * @returns Promise<string> 服务端相对路径，如 /uploads/2026-09-14/xxx.pdf
 */
export function uploadFile(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (evt) => {
      if (onProgress && evt.total) {
        onProgress(Math.round((evt.loaded / evt.total) * 100))
      }
    },
  })
}
