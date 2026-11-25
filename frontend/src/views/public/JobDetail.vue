<template>
  <div v-loading="loading" class="job-detail">
    <PageHeader>
      <template #title>
        <span>{{ job?.jobTitle }}</span>
      </template>
      <template #actions>
        <el-button @click="$router.back()">返回</el-button>
      </template>
    </PageHeader>

    <el-card v-if="job" class="info-card">
      <div class="job-title-row">
        <h2>{{ job.jobTitle }}</h2>
        <el-tag v-if="job.auditState === 'online'" type="success" size="large">在招</el-tag>
      </div>
      <div class="salary">{{ formatMoney(job.benefitMoney) }} / 月</div>

      <div class="info-grid">
        <div class="info-item">
          <el-icon><OfficeBuilding /></el-icon>
          <router-link :to="`/companies/${job.companyId}`">{{ job.companyName }}</router-link>
          <el-tag v-if="job.companyAuthState === 'auth'" type="success" size="small">已认证</el-tag>
        </div>
        <div class="info-item">
          <el-icon><Briefcase /></el-icon>
          <DictTag type-code="job_type" :code="job.jobType" />
        </div>
        <div class="info-item">
          <el-icon><Location /></el-icon>
          <span>{{ job.workPlace || '不限' }}</span>
        </div>
        <div v-if="job.category" class="info-item">
          <el-icon><Collection /></el-icon>
          <el-tag size="small">{{ job.category }}</el-tag>
        </div>
        <div class="info-item">
          <el-icon><User /></el-icon>
          <span>招聘 {{ job.headcount }} 人</span>
        </div>
        <div class="info-item">
          <el-icon><Calendar /></el-icon>
          <span>{{ formatDate(job.startDate) }} ~ {{ formatDate(job.endDate) }}</span>
        </div>
      </div>

      <el-divider />

      <h3>岗位职责</h3>
      <pre class="content-pre">{{ job.duty || '暂无' }}</pre>

      <h3>任职要求</h3>
      <pre class="content-pre">{{ job.req || '暂无' }}</pre>

      <div class="apply-actions">
        <el-button
          v-if="authStore.isLoggedIn && authStore.userType === 'student'"
          size="large"
          :type="job.applied ? 'info' : 'primary'"
          :disabled="job.applied"
          @click="goSearch"
        >
          {{ job.applied ? '已投递' : '前往岗位检索页投递' }}
        </el-button>
        <el-button v-if="!authStore.isLoggedIn" type="primary" size="large" @click="$router.push('/login')">
          登录后投递
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { jobDetail } from '@/api/job'
import { formatMoney, formatDate } from '@/utils/format'
import PageHeader from '@/components/PageHeader.vue'
import DictTag from '@/components/DictTag.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const job = ref(null)
const loading = ref(false)

function goSearch() {
  router.push('/jobs')
}

async function load() {
  const id = route.params.id
  loading.value = true
  try {
    job.value = await jobDetail(id)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.job-detail {
  min-height: 400px;
}
.info-card {
  padding: 4px;
}
.job-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.job-title-row h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}
.salary {
  margin: 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}
h3 {
  margin: 16px 0 8px;
  font-size: 16px;
  color: #303133;
}
.content-pre {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #606266;
  font-family: inherit;
}
.apply-actions {
  margin-top: 32px;
  text-align: center;
}
</style>
