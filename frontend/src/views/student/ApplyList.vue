<template>
  <div class="apply-list">
    <PageHeader title="我的投递" desc="查看投递记录和状态进展" />

    <div class="filter-bar">
      <el-radio-group v-model="query.applyState" @change="handleSearch">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="pending">待处理</el-radio-button>
        <el-radio-button label="interview">进入面试</el-radio-button>
        <el-radio-button label="rejected">未通过</el-radio-button>
        <el-radio-button label="passed">已通过</el-radio-button>
      </el-radio-group>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="list" border>
        <el-table-column label="岗位" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/jobs/${row.jobId}`">{{ row.jobTitle }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="companyName" label="企业" min-width="160" />
        <el-table-column prop="workPlace" label="工作地" width="120" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <StateBadge :state="row.applyState" :map="APPLY_STATE_MAP" />
          </template>
        </el-table-column>
        <el-table-column label="投递时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.applyState === 'pending'"
              size="small"
              type="warning"
              @click="handleWithdraw(row)"
            >
              撤回
            </el-button>
            <el-button
              v-if="row.applyState === 'interview'"
              size="small"
              type="primary"
              @click="$router.push('/student/interviews')"
            >
              查看面试
            </el-button>
            <el-button size="small" @click="$router.push(`/jobs/${row.jobId}`)">
              岗位详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && !list.length" text="还没有投递记录" />
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
import { myApplies } from '@/api/apply'
import { withdrawApply } from '@/api/apply'
import { usePagination } from '@/composables/usePagination'
import { formatDateTime } from '@/utils/format'
import { APPLY_STATE_MAP } from '@/utils/enums'
import { success } from '@/utils/message'
import { ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import EmptyState from '@/components/EmptyState.vue'

const { query, list, total, loading, fetchList, handlePageChange, handleSizeChange, handleSearch } =
  usePagination(myApplies, { applyState: '', current: 1, size: 10 })

async function handleWithdraw(row) {
  try {
    await ElMessageBox.confirm(`确定撤回「${row.jobTitle}」的投递吗？`, '提示', {
      confirmButtonText: '撤回',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await withdrawApply(row.applyId)
  success('已撤回投递')
  fetchList()
}

import { onMounted } from 'vue'
onMounted(fetchList)
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
