<template>
  <el-tag v-if="label" :type="tagType" size="small">{{ label }}</el-tag>
  <span v-else>{{ code || '-' }}</span>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useDictStore } from '@/stores/dict'

const props = defineProps({
  typeCode: { type: String, required: true },
  code: { type: String, default: '' },
  tagType: { type: String, default: '' }, // 可选覆盖
})

const dictStore = useDictStore()

async function ensureLoaded() {
  if (!dictStore.get(props.typeCode).length) {
    await dictStore.load(props.typeCode)
  }
}
ensureLoaded().catch(() => {})

const label = computed(() => dictStore.label(props.typeCode, props.code))

const tagType = computed(() => {
  if (props.tagType) return props.tagType
  // 默认按 code 类型映射一些常见颜色
  const code = props.code
  if (['online', 'signed', 'done', 'passed', 'auth', 'reviewed'].includes(code)) return 'success'
  if (['pending', 'confirmed', 'submitted', 'draft'].includes(code)) return 'info'
  if (['interview'].includes(code)) return 'warning'
  if (['rejected', 'cancelled', 'reject', 'freeze', 'offline'].includes(code)) return 'danger'
  return 'info'
})

watch(
  () => props.typeCode,
  () => ensureLoaded().catch(() => {})
)
</script>
