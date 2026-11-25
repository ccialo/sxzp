<template>
  <div class="file-preview">
    <template v-if="isImage">
      <el-image
        :src="fullPath"
        :preview-src-list="[fullPath]"
        fit="contain"
        style="max-width: 200px; max-height: 200px"
      />
    </template>
    <template v-else-if="isPdf">
      <el-link type="primary" :href="fullPath" target="_blank">
        <el-icon><Document /></el-icon>
        <span>{{ fileName || '查看文件' }}</span>
      </el-link>
    </template>
    <template v-else>
      <el-link type="primary" :href="fullPath" target="_blank" :download="fileName">
        <el-icon><Download /></el-icon>
        <span>{{ fileName || '下载文件' }}</span>
      </el-link>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  path: { type: String, default: '' },
  fileName: { type: String, default: '' },
})

const fullPath = computed(() => props.path || '')

const ext = computed(() => {
  if (!props.path) return ''
  const m = props.path.match(/\.(\w+)$/)
  return (m ? m[1] : '').toLowerCase()
})

const isImage = computed(() => ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext.value))
const isPdf = computed(() => ext.value === 'pdf')
</script>

<style scoped>
.file-preview {
  display: inline-flex;
  align-items: center;
}
</style>
