<template>
  <div class="app-sidebar">
    <div class="brand">
      <el-icon :size="24"><Briefcase /></el-icon>
      <span v-if="!isCollapse" class="brand-text">校园实习招聘</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :unique-opened="true"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
    >
      <el-menu-item
        v-for="item in menus"
        :key="item.path"
        :index="item.path"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <template #title>{{ item.title }}</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { MENUS } from '@/utils/menus'

const props = defineProps({
  isCollapse: { type: Boolean, default: false },
})

const route = useRoute()
const authStore = useAuthStore()

const menus = computed(() => MENUS[authStore.userType] || [])
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.app-sidebar {
  height: 100vh;
  background: #304156;
}
.brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  background: #2b3a4d;
}
.brand-text {
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}
.el-menu {
  border-right: none;
}
</style>
