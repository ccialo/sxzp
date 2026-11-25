<template>
  <div ref="chartRef" class="chart-base" :style="{ width: '100%', height: height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '320px' },
})

const chartRef = ref(null)
let chart = null
let resizeRaf = null

function render() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  chart.setOption(props.option, true)
}

function handleResize() {
  // 用 rAF 合并连续 resize,避免图表重排触发浏览器的 ResizeObserver 循环告警
  if (resizeRaf) return
  resizeRaf = requestAnimationFrame(() => {
    resizeRaf = null
    chart?.resize()
  })
}

onMounted(async () => {
  await nextTick()
  render()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (resizeRaf) {
    cancelAnimationFrame(resizeRaf)
    resizeRaf = null
  }
  chart?.dispose()
  chart = null
})

watch(
  () => props.option,
  () => render(),
  { deep: true }
)
</script>

<style scoped>
.chart-base {
  min-height: 200px;
}
</style>
