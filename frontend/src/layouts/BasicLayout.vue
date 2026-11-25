<template>
  <el-container class="basic-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <AppSidebar :is-collapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header class="header">
        <AppHeader v-model:is-collapse="isCollapse" />
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import AppSidebar from './components/AppSidebar.vue'
import AppHeader from './components/AppHeader.vue'

const isCollapse = ref(false)
</script>

<style scoped>
.basic-layout {
  height: 100vh;
}
.sidebar {
  background: #304156;
  transition: width 0.28s;
  overflow: hidden;
}
.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0;
  display: flex;
  align-items: center;
  height: 60px;
}
.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
