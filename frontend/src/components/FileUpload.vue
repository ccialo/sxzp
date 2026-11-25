<template>
  <el-upload
    :show-file-list="false"
    :http-request="customUpload"
    :before-upload="beforeUpload"
    :accept="accept"
  >
    <el-button :icon="Upload" :loading="uploading">
      {{ uploading ? `上传中 ${progress}%` : '点击上传' }}
    </el-button>
    <template v-if="modelValue">
      <FilePreview :path="modelValue" class="preview" :file-name="currentName" />
      <el-button text type="danger" :icon="Delete" @click.stop="clear" />
    </template>
  </el-upload>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Upload, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/file'
import FilePreview from './FilePreview.vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  accept: { type: String, default: '.jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.xls,.xlsx,.zip,.rar' },
  maxSize: { type: Number, default: 10 }, // MB
})

const emit = defineEmits(['update:modelValue'])

const uploading = ref(false)
const progress = ref(0)
const currentName = ref('')

function beforeUpload(file) {
  if (file.size > props.maxSize * 1024 * 1024) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

async function customUpload({ file }) {
  uploading.value = true
  progress.value = 0
  try {
    const path = await uploadFile(file, (p) => {
      progress.value = p
    })
    currentName.value = file.name
    emit('update:modelValue', path)
    ElMessage.success('上传成功')
  } catch {
    // 拦截器已弹错误
  } finally {
    uploading.value = false
    progress.value = 0
  }
}

function clear() {
  currentName.value = ''
  emit('update:modelValue', '')
}
</script>

<style scoped>
.preview {
  margin-left: 12px;
}
</style>
