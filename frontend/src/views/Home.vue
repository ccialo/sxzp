<template>
  <div class="home-page">
    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">校园实习招聘管理平台</h1>
        <p class="hero-subtitle">连接学生与企业，开启职业第一步</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/jobs')">
            立即检索岗位
          </el-button>
          <el-button size="large" class="screen-entry" @click="$router.push('/screen')">
            <el-icon><DataAnalysis /></el-icon>
            数据大屏
          </el-button>
          <el-button v-if="!authStore.isLoggedIn" size="large" @click="$router.push('/register')">
            注册账号
          </el-button>
          <el-button v-else size="large" @click="goHome">进入主页</el-button>
        </div>
      </div>
    </section>
    <section class="features">
      <div class="features-inner">
        <div v-for="f in features" :key="f.title" class="feature-card">
          <el-icon :size="40" class="feature-icon"><component :is="f.icon" /></el-icon>
          <h3>{{ f.title }}</h3>
          <p>{{ f.desc }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getHomeRoute } from '@/permission'

const router = useRouter()
const authStore = useAuthStore()

const features = [
  { title: '岗位检索', desc: '关键词、类型、地点多维度筛选，找到心仪实习', icon: 'Search' },
  { title: '简历管理', desc: '在线编辑多份简历，灵活切换投递', icon: 'Document' },
  { title: '面试安排', desc: '企业发起面试通知，学生一键确认', icon: 'Calendar' },
  { title: '协议签署', desc: '实习协议线上发起与签署，留痕可追溯', icon: 'Files' },
  { title: '实习周报', desc: '学生提交周报，企业在线评价', icon: 'EditPen' },
  { title: '消息通知', desc: '投递、面试、协议变更全程站内信提醒', icon: 'Bell' },
]

function goHome() {
  router.push(getHomeRoute(authStore.userType))
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.hero {
  height: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #fff;
}
.hero-title {
  font-size: 44px;
  font-weight: 700;
  margin: 0 0 16px;
}
.hero-subtitle {
  font-size: 18px;
  margin: 0 0 32px;
  opacity: 0.9;
}
.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
.features {
  background: #f5f7fa;
  padding: 48px 24px;
}
.features-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.feature-card {
  background: #fff;
  padding: 32px 24px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}
.feature-card:hover {
  transform: translateY(-4px);
}
.feature-icon {
  color: #409eff;
  margin-bottom: 12px;
}
.feature-card h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #303133;
}
.feature-card p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}
@media (max-width: 768px) {
  .features-inner {
    grid-template-columns: 1fr;
  }
}
</style>
