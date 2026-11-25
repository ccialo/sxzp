<template>
  <div class="interview-sent">
    <PageHeader title="已发出面试" desc="管理已安排的面试" />

    <div class="filter-bar">
      <el-radio-group v-model="query.state" @change="handleSearch">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="pending">待确认</el-radio-button>
        <el-radio-button label="confirmed">已确认</el-radio-button>
        <el-radio-button label="done">已完成</el-radio-button>
        <el-radio-button label="cancelled">已取消</el-radio-button>
      </el-radio-group>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="studentName" label="学生" min-width="100" />
        <el-table-column prop="jobTitle" label="岗位" min-width="160" />
        <el-table-column label="面试时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.interviewTime) }}</template>
        </el-table-column>
        <el-table-column prop="place" label="地点" min-width="140" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StateBadge :state="row.state" :map="INTERVIEW_STATE_MAP" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.state === 'confirmed'"
              size="small"
              type="success"
              @click="handleDone(row)"
            >
              标记完成
            </el-button>
            <el-button
              v-if="row.state === 'pending' || row.state === 'confirmed'"
              size="small"
              type="danger"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.state === 'done'"
              size="small"
              type="primary"
              @click="openAgreement(row)"
            >
              发起协议
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && !list.length" text="暂无面试安排" />
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

    <AgreementCreateDialog
      v-model="agreementVisible"
      :apply-id="current?.applyId"
      @success="onAgreementSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { sentInterviews, doneInterview, cancelInterview } from '@/api/interview'
import { usePagination } from '@/composables/usePagination'
import { formatDateTime } from '@/utils/format'
import { INTERVIEW_STATE_MAP } from '@/utils/enums'
import { success } from '@/utils/message'
import { ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import EmptyState from '@/components/EmptyState.vue'
import AgreementCreateDialog from './components/AgreementCreateDialog.vue'

const { query, list, total, loading, fetchList, handlePageChange, handleSizeChange, handleSearch } =
  usePagination(sentInterviews, { state: '', current: 1, size: 10 })

const agreementVisible = ref(false)
const current = ref(null)

async function handleDone(row) {
  try {
    await ElMessageBox.confirm('确定将这场面试标记为已完成吗？', '提示', {
      confirmButtonText: '已完成',
      cancelButtonText: '取消',
      type: 'success',
    })
  } catch {
    return
  }
  await doneInterview(row.interviewId)
  success('面试已完成')
  fetchList()
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定取消这场面试吗？', '提示', {
      confirmButtonText: '取消面试',
      cancelButtonText: '保留',
      type: 'warning',
    })
  } catch {
    return
  }
  await cancelInterview(row.interviewId)
  success('已取消面试')
  fetchList()
}

function openAgreement(row) {
  current.value = row
  agreementVisible.value = true
}

function onAgreementSuccess() {
  agreementVisible.value = false
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
