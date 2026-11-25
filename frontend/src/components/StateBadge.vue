<template>
  <el-tag :type="badge.type" size="small">{{ badge.label }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  state: { type: String, default: '' },
  map: { type: Object, required: true },
})

const badge = computed(() => {
  return props.map[props.state] || { label: props.state || '-', type: 'info' }
})
</script>
