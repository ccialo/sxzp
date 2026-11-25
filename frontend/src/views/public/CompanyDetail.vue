<template>
  <div v-loading="loading" class="company-detail">
    <PageHeader>
      <template #title>
        <span>{{ company?.entName }}</span>
      </template>
      <template #actions>
        <el-button @click="$router.back()">返回</el-button>
      </template>
    </PageHeader>

    <el-card v-if="company" class="info-card">
      <div class="header">
        <div class="title-row">
          <el-icon :size="32" color="#409eff"><OfficeBuilding /></el-icon>
          <h2>{{ company.entName }}</h2>
          <StateBadge
            v-if="company.authState"
            :state="company.authState"
            :map="COMPANY_AUTH_STATE_MAP"
          />
        </div>
      </div>

      <div class="info-grid">
        <div v-if="company.category" class="info-item">
          <el-icon><Collection /></el-icon>
          <DictTag type-code="company_category" :code="company.category" />
        </div>
        <div v-if="company.address" class="info-item">
          <el-icon><Location /></el-icon>
          <span>{{ company.address }}</span>
        </div>
        <div v-if="company.contactName" class="info-item">
          <el-icon><User /></el-icon>
          <span>联系人：{{ company.contactName }}</span>
        </div>
        <div v-if="company.contactPhone" class="info-item">
          <el-icon><Phone /></el-icon>
          <span>{{ formatPhone(company.contactPhone) }}</span>
        </div>
        <div v-if="company.contactEmail" class="info-item">
          <el-icon><Message /></el-icon>
          <span>{{ company.contactEmail }}</span>
        </div>
      </div>

      <el-divider />

      <h3>企业简介</h3>
      <pre class="content-pre">{{ company.intro || '暂无简介' }}</pre>

      <div v-if="company.certFile" class="cert">
        <h3>资质证明</h3>
        <FilePreview :path="company.certFile" file-name="资质文件" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { detail } from '@/api/company'
import { formatPhone } from '@/utils/format'
import { COMPANY_AUTH_STATE_MAP } from '@/utils/enums'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import DictTag from '@/components/DictTag.vue'
import FilePreview from '@/components/FilePreview.vue'

const route = useRoute()
const company = ref(null)
const loading = ref(false)

async function load() {
  const id = route.params.id
  loading.value = true
  try {
    company.value = await detail(id)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.info-card {
  padding: 4px;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title-row h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 16px;
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
.cert {
  margin-top: 24px;
}
</style>
