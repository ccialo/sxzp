<template>
  <el-empty :description="text" :image-size="imageSize">
    <slot />
  </el-empty>
</template>

<script setup>
defineProps({
  text: { type: String, default: '暂无数据' },
  imageSize: { type: Number, default: 100 },
})
</script>
