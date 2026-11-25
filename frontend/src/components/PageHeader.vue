<template>
  <div class="page-header">
    <div class="header-left">
      <h2 class="title"><slot name="title">{{ title }}</slot></h2>
      <p v-if="desc || $slots.desc" class="desc">
        <slot name="desc">{{ desc }}</slot>
      </p>
    </div>
    <div class="header-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  desc: { type: String, default: '' },
})
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.title {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
.desc {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}
</style>
