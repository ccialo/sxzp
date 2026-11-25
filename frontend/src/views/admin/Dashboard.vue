<template>
  <div v-loading="loading" class="dashboard">
    <PageHeader title="仪表盘" desc="平台运营数据总览" />

    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">学生总数</div>
          <div class="stat-value">{{ data.studentCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">企业总数</div>
          <div class="stat-value">{{ data.companyCount || 0 }}</div>
          <div class="stat-sub">已认证 {{ data.companyAuthedCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">岗位总数</div>
          <div class="stat-value">{{ data.jobCount || 0 }}</div>
          <div class="stat-sub">在线 {{ data.jobOnlineCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">投递总数</div>
          <div class="stat-value">{{ data.applyCount || 0 }}</div>
          <div class="stat-sub">今日 {{ data.todayApplyCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">面试总数</div>
          <div class="stat-value">{{ data.interviewCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">协议总数</div>
          <div class="stat-value">{{ data.agreementCount || 0 }}</div>
          <div class="stat-sub">已签 {{ data.agreementSignedCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>投递状态分布</template>
          <ChartBase v-if="applyStateOption" :option="applyStateOption" />
          <EmptyState v-else text="暂无数据" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>面试状态分布</template>
          <ChartBase v-if="interviewStateOption" :option="interviewStateOption" />
          <EmptyState v-else text="暂无数据" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>周报统计</template>
          <div class="weekly-stats">
            <div>周报总数：{{ data.weeklyReportCount || 0 }}</div>
            <div>已评价：{{ data.weeklyReviewedCount || 0 }}</div>
            <div>评价率：{{ weeklyRate }}%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dashboard } from '@/api/stats'
import { APPLY_STATE_MAP, INTERVIEW_STATE_MAP } from '@/utils/enums'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import ChartBase from '@/components/ChartBase.vue'

const loading = ref(false)
const data = ref({})

const weeklyRate = computed(() => {
  const total = data.value.weeklyReportCount || 0
  const reviewed = data.value.weeklyReviewedCount || 0
  if (!total) return 0
  return Math.round((reviewed / total) * 100)
})

function buildPieOption(map, countMap) {
  if (!countMap) return null
  const items = Object.entries(countMap).map(([k, v]) => ({
    name: map[k]?.label || k,
    value: v,
  }))
  if (!items.length) return null
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
        data: items,
      },
    ],
  }
}

const applyStateOption = computed(() =>
  buildPieOption(APPLY_STATE_MAP, data.value.applyStateCount)
)
const interviewStateOption = computed(() =>
  buildPieOption(INTERVIEW_STATE_MAP, data.value.interviewStateCount)
)

async function load() {
  loading.value = true
  try {
    data.value = await dashboard()
  } catch {
    // 错误提示已由 request.js 拦截器统一处理
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.stat-cards {
  margin-bottom: 16px;
}
.stat-card {
  margin-bottom: 16px;
  text-align: center;
}
.stat-title {
  font-size: 13px;
  color: #909399;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 8px 0;
}
.stat-sub {
  font-size: 12px;
  color: #909399;
}
.chart-row {
  margin-bottom: 16px;
}
.weekly-stats {
  font-size: 14px;
  color: #606266;
  line-height: 2;
  padding: 16px 0;
}
</style>
