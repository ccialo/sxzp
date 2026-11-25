<template>
  <el-card class="job-card" shadow="hover" :body-style="{ padding: '20px' }" @click="$emit('click')">
    <div class="card-header">
      <div class="title-row">
        <h3 class="job-title">{{ job.jobTitle }}</h3>
        <el-tag v-if="job.auditState === 'online'" type="success" size="small">在招</el-tag>
      </div>
      <div class="company">
        <el-icon><OfficeBuilding /></el-icon>
        <router-link :to="`/companies/${job.companyId}`" class="company-link" @click.stop>
          {{ job.companyName }}
        </router-link>
        <el-tag v-if="job.companyAuthState === 'auth'" type="success" size="small">已认证</el-tag>
      </div>
    </div>
    <div class="card-body">
      <div class="info-row">
        <el-icon><Briefcase /></el-icon>
        <DictTag type-code="job_type" :code="job.jobType" />
      </div>
      <div class="info-row">
        <el-icon><Location /></el-icon>
        <span>{{ job.workPlace || '不限' }}</span>
      </div>
      <div class="info-row">
        <el-icon><Money /></el-icon>
        <span class="salary">{{ formatMoney(job.benefitMoney) }} / 月</span>
      </div>
      <div v-if="job.category" class="info-row">
        <el-icon><Collection /></el-icon>
        <el-tag size="small">{{ job.category }}</el-tag>
      </div>
    </div>
    <div class="card-footer">
      <span class="publish-time">发布于 {{ formatDateTime(job.publishTime, 'YYYY-MM-DD') }}</span>
      <div class="actions">
        <slot name="actions" />
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { formatMoney, formatDateTime } from '@/utils/format'
import DictTag from './DictTag.vue'

defineProps({
  job: { type: Object, required: true },
})
defineEmits(['click'])
</script>

<style scoped>
.job-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.job-card:hover {
  transform: translateY(-2px);
}
.card-header {
  margin-bottom: 12px;
}
.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.job-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
  cursor: pointer;
}
.job-title:hover {
  color: #409eff;
}
.company {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}
.company-link {
  color: #606266;
}
.company-link:hover {
  color: #409eff;
}
.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}
.info-row {
  display: flex;
  align-items: center;
  gap: 6px;
}
.salary {
  color: #f56c6c;
  font-weight: 600;
}
.card-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.publish-time {
  font-size: 12px;
  color: #909399;
}
.actions {
  display: flex;
  gap: 8px;
}
</style>
