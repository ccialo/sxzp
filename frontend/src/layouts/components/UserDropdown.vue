<template>
  <el-dropdown class="user-dropdown" @command="handleCommand">
    <div class="trigger">
      <el-avatar :size="32" class="avatar">
        {{ avatarText }}
      </el-avatar>
      <span class="name">{{ authStore.realName }}</span>
      <el-icon><CaretBottom /></el-icon>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item disabled>
          {{ roleLabel }}
        </el-dropdown-item>
        <el-dropdown-item divided command="home">
          <el-icon><HomeFilled /></el-icon>进入主页
        </el-dropdown-item>
        <el-dropdown-item command="jobs">
          <el-icon><Search /></el-icon>岗位检索
        </el-dropdown-item>
        <el-dropdown-item divided command="logout">
          <el-icon><SwitchButton /></el-icon>退出登录
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { ROLE_LABELS } from '@/utils/menus'
import { getHomeRoute } from '@/permission'

const authStore = useAuthStore()
const router = useRouter()

const avatarText = computed(() => {
  const name = authStore.realName || authStore.user?.account || 'U'
  return name.charAt(0).toUpperCase()
})

const roleLabel = computed(() => {
  return ROLE_LABELS[authStore.userType] || '未知'
})

async function handleCommand(command) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
    } catch {
      return
    }
    await authStore.logout()
    router.push('/login')
  } else if (command === 'home') {
    router.push(getHomeRoute(authStore.userType))
  } else if (command === 'jobs') {
    router.push('/jobs')
  }
}
</script>

<style scoped>
.user-dropdown {
  cursor: pointer;
}
.trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 8px;
  height: 60px;
  outline: none;
}
.avatar {
  background: #409eff;
  color: #fff;
}
.name {
  font-size: 14px;
  color: #303133;
}
</style>
