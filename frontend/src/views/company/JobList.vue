<template>
  <div class="job-list">
    <PageHeader title="我的岗位" desc="管理已发布的实习岗位">
      <template #actions>
        <el-button type="primary" @click="$router.push('/company/jobs/edit')">
          <el-icon><Plus /></el-icon>发布岗位
        </el-button>
      </template>
    </PageHeader>

    <div class="filter-bar">
      <el-radio-group v-model="query.auditState" @change="handleSearch">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="draft">草稿</el-radio-button>
        <el-radio-button label="pending">待审核</el-radio-button>
        <el-radio-button label="online">已上线</el-radio-button>
        <el-radio-button label="reject">已驳回</el-radio-button>
        <el-radio-button label="ended">已下线</el-radio-button>
      </el-radio-group>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="jobTitle" label="岗位名称" min-width="200" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <DictTag type-code="job_type" :code="row.jobType" />
          </template>
        </el-table-column>
        <el-table-column prop="headcount" label="招聘人数" width="100" />
        <el-table-column prop="workPlace" label="工作地" width="120" />
        <el-table-column label="津贴" width="100">
          <template #default="{ row }">{{ formatMoney(row.benefitMoney) }}</template>
        </el-table-column>
        <el-table-column prop="applyCount" label="投递数" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              v-if="row.auditState === 'ended'"
              type="danger"
              effect="plain"
              size="small"
            >
              已下线
            </el-tag>
            <StateBadge v-else :state="row.auditState" :map="JOB_AUDIT_STATE_MAP" />
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="120">
          <template #default="{ row }">{{ formatDate(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/company/jobs/edit?id=${row.jobId}`)">
              编辑
            </el-button>
            <el-button
              v-if="row.auditState === 'reject'"
              size="small"
              type="warning"
              @click="handleResubmit(row)"
            >
              重新提交
            </el-button>
            <el-button
              v-if="row.auditState === 'online'"
              size="small"
              type="danger"
              @click="handleOffline(row)"
            >
              下线
            </el-button>
            <el-button
              v-if="row.auditState === 'ended'"
              size="small"
              type="primary"
              @click="handleResubmit(row)"
            >
              发送审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && !list.length" text="还没有发布岗位" />
    </el-card>

    <div v-if="total > 0" class="pagination">
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { myJobs, offlineJob, resubmitJob } from '@/api/job'
import { usePagination } from '@/composables/usePagination'
import { formatMoney, formatDate } from '@/utils/format'
import { JOB_AUDIT_STATE_MAP } from '@/utils/enums'
import { success } from '@/utils/message'
import { ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import DictTag from '@/components/DictTag.vue'
import EmptyState from '@/components/EmptyState.vue'

const { query, list, total, loading, fetchList, handlePageChange, handleSizeChange, handleSearch } =
  usePagination(myJobs, { auditState: '', current: 1, size: 10 })

async function handleResubmit(row) {
  await resubmitJob(row.jobId)
  success('已提交审核')
  fetchList()
}

async function handleOffline(row) {
  try {
    await ElMessageBox.confirm(`确定下线「${row.jobTitle}」吗？`, '提示', {
      confirmButtonText: '下线',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await offlineJob(row.jobId)
  success('岗位已下线')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
