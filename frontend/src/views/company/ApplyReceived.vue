<template>
  <div class="apply-received">
    <PageHeader title="收到的投递" desc="筛选并处理学生投递" />

    <div class="filter-bar">
      <el-select v-model="query.jobId" placeholder="选择岗位" clearable style="width: 220px" @change="handleSearch">
        <el-option
          v-for="j in jobOptions"
          :key="j.jobId"
          :label="j.jobTitle"
          :value="j.jobId"
        />
      </el-select>
      <el-radio-group v-model="query.applyState" @change="handleSearch">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="pending">待处理</el-radio-button>
        <el-radio-button label="interview">进入面试</el-radio-button>
        <el-radio-button label="rejected">已淘汰</el-radio-button>
        <el-radio-button label="passed">已通过</el-radio-button>
      </el-radio-group>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="studentName" label="学生姓名" min-width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="college" label="学院" min-width="140" />
        <el-table-column prop="major" label="专业" min-width="140" />
        <el-table-column prop="jobTitle" label="投递岗位" min-width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StateBadge :state="row.applyState" :map="APPLY_STATE_MAP" />
          </template>
        </el-table-column>
        <el-table-column label="投递时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showResume(row)">查看简历</el-button>
            <el-button
              v-if="row.applyState === 'pending'"
              size="small"
              type="success"
              @click="handleAudit(row, 'interview')"
            >
              安排面试
            </el-button>
            <el-button
              v-if="row.applyState === 'pending'"
              size="small"
              type="danger"
              @click="handleAudit(row, 'rejected')"
            >
              淘汰
            </el-button>
            <el-button
              v-if="row.applyState === 'interview'"
              size="small"
              type="primary"
              @click="openSchedule(row)"
            >
              面试安排
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && !list.length" text="暂无投递记录" />
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

    <!-- 简历快照对话框 -->
    <el-dialog v-model="resumeVisible" title="简历内容" width="640px">
      <ResumePreview
        v-if="current && current.resumeSnapshot"
        :resume="{}"
        :snapshot="current.resumeSnapshot"
      />
      <EmptyState v-else text="该投递无简历快照" />
      <template #footer>
        <el-button @click="resumeVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 安排面试对话框 -->
    <InterviewScheduleDialog
      v-model="scheduleVisible"
      :apply-id="current?.applyId"
      @success="onScheduleSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { receivedApplies, auditApply } from '@/api/apply'
import { myJobs } from '@/api/job'
import { usePagination } from '@/composables/usePagination'
import { formatDateTime } from '@/utils/format'
import { APPLY_STATE_MAP } from '@/utils/enums'
import { success } from '@/utils/message'
import { ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import EmptyState from '@/components/EmptyState.vue'
import ResumePreview from '@/components/ResumePreview.vue'
import InterviewScheduleDialog from './components/InterviewScheduleDialog.vue'

const jobOptions = ref([])

const { query, list, total, loading, fetchList, handlePageChange, handleSizeChange, handleSearch } =
  usePagination(receivedApplies, { jobId: null, applyState: '', current: 1, size: 10 })

const resumeVisible = ref(false)
const scheduleVisible = ref(false)
const current = ref(null)

function showResume(row) {
  current.value = row
  resumeVisible.value = true
}

function openSchedule(row) {
  current.value = row
  scheduleVisible.value = true
}

async function handleAudit(row, state) {
  const action = state === 'interview' ? '安排面试' : '淘汰'
  try {
    await ElMessageBox.confirm(`确定对「${row.studentName}」${action}吗？`, '提示', {
      confirmButtonText: action,
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await auditApply({ applyId: row.applyId, applyState: state })
  success(`${action}成功`)
  if (state === 'interview') {
    openSchedule(row)
  }
  fetchList()
}

function onScheduleSuccess() {
  scheduleVisible.value = false
  fetchList()
}

onMounted(async () => {
  try {
    const data = await myJobs({ current: 1, size: 100 })
    jobOptions.value = data?.records || []
  } catch {
    // 忽略
  }
  fetchList()
})
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
