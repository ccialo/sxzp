<template>
  <el-badge :value="count" :hidden="count === 0" :max="99" class="notice-bell">
    <el-icon :size="20" class="bell-icon" @click="goNoticeList">
      <Bell />
    </el-icon>
  </el-badge>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { unreadCount } from '@/api/notice'

const authStore = useAuthStore()
const router = useRouter()
const count = ref(0)
let timer = null

async function refresh() {
  if (!authStore.isLoggedIn) {
    count.value = 0
    return
  }
  try {
    const num = await unreadCount()
    count.value = Number(num) || 0
  } catch {
    // 静默失败
  }
}

function goNoticeList() {
  const type = authStore.userType
  if (type === 'student') router.push('/student/notices')
  else if (type === 'company') router.push('/company/notices')
  else router.push('/admin/dashboard')
}

onMounted(() => {
  refresh()
  // 30 秒轮询
  timer = setInterval(refresh, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

defineExpose({ refresh })
</script>

<style scoped>
.notice-bell {
  cursor: pointer;
}
.bell-icon {
  color: #5a5e66;
  cursor: pointer;
}
.bell-icon:hover {
  color: #409eff;
}
</style>
