<template>
  <div class="public-layout">
    <header class="public-header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="24"><Briefcase /></el-icon>
          <span>校园实习招聘平台</span>
        </div>
        <nav class="nav">
          <router-link to="/jobs">岗位检索</router-link>
        </nav>
        <div class="actions">
          <template v-if="authStore.isLoggedIn">
            <el-button text @click="goHome">进入主页</el-button>
            <el-button type="primary" @click="logout">退出登录</el-button>
          </template>
          <template v-else>
            <el-button text @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>
    <main class="public-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { getHomeRoute } from '@/permission'

const authStore = useAuthStore()
const router = useRouter()

function goHome() {
  router.push(getHomeRoute(authStore.userType))
}

async function logout() {
  await authStore.logout()
  router.push('/jobs')
}
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  background: #f5f7fa;
}
.public-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  cursor: pointer;
}
.nav {
  margin-left: 40px;
  display: flex;
  gap: 24px;
}
.nav a {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
}
.nav a.router-link-active {
  color: #409eff;
}
.actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}
.public-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 60px);
}
</style>
