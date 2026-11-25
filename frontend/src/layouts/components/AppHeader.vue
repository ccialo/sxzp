<template>
  <div class="app-header">
    <div class="left">
      <el-icon class="collapse-btn" :size="20" @click="toggleCollapse">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
      <span class="title">{{ pageTitle }}</span>
    </div>
    <div class="right">
      <NoticeBell />
      <UserDropdown />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ROLE_LABELS } from '@/utils/menus'
import NoticeBell from './NoticeBell.vue'
import UserDropdown from './UserDropdown.vue'

const props = defineProps({
  isCollapse: { type: Boolean, default: false },
})
const emit = defineEmits(['update:isCollapse'])

const route = useRoute()
const authStore = useAuthStore()

const pageTitle = computed(() => {
  if (route.meta.title) return route.meta.title
  return '校园实习招聘管理平台'
})

function toggleCollapse() {
  emit('update:isCollapse', !props.isCollapse)
}
</script>

<style scoped>
.app-header {
  width: 100%;
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.collapse-btn {
  cursor: pointer;
  color: #5a5e66;
}
.collapse-btn:hover {
  color: #409eff;
}
.title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
.right {
  display: flex;
  align-items: center;
  gap: 16px;
}
</style>
