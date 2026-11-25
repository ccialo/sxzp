<template>
  <div class="job-audit">
    <PageHeader title="岗位审核" desc="审核企业发布的岗位" />

    <div class="filter-bar">
      <el-radio-group v-model="query.auditState" @change="handleSearch">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="draft">草稿</el-radio-button>
        <el-radio-button label="pending">待审核</el-radio-button>
        <el-radio-button label="online">已上线</el-radio-button>
        <el-radio-button label="reject">已驳回</el-radio-button>
        <el-radio-button label="ended">已下线</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="query.keyword"
        placeholder="岗位名称 / 企业名称"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="jobTitle" label="岗位名称" min-width="200" />
        <el-table-column prop="companyName" label="企业" min-width="160" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <DictTag type-code="job_type" :code="row.jobType" />
          </template>
        </el-table-column>
        <el-table-column prop="headcount" label="招聘数" width="80" />
        <el-table-column prop="workPlace" label="工作地" width="120" />
        <el-table-column label="津贴" width="100">
          <template #default="{ row }">{{ formatMoney(row.benefitMoney) }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <StateBadge :state="row.auditState" :map="JOB_AUDIT_STATE_MAP" />
          </template>
        </el-table-column>
        <el-table-column prop="auditOpinion" label="驳回意见" min-width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.auditState === 'pending'"
              size="small"
              type="primary"
              @click="openAudit(row)"
            >
              审核
            </el-button>
            <el-button size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && !list.length" text="暂无岗位记录" />
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

    <AuditDialog
      v-model="auditVisible"
      title="审核岗位"
      @confirm="handleAudit"
    />

    <el-dialog v-model="detailVisible" title="岗位详情" width="640px">
      <template v-if="current">
        <h2>{{ current.jobTitle }}</h2>
        <p><strong>企业：</strong>{{ current.companyName }}</p>
        <p><strong>招聘人数：</strong>{{ current.headcount }}</p>
        <p><strong>工作地点：</strong>{{ current.workPlace }}</p>
        <p><strong>津贴：</strong>{{ formatMoney(current.benefitMoney) }}/月</p>
        <h4>岗位职责</h4>
        <pre class="content-pre">{{ current.duty || '暂无' }}</pre>
        <h4>任职要求</h4>
        <pre class="content-pre">{{ current.req || '暂无' }}</pre>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminJobPage, auditJob } from '@/api/job'
import { usePagination } from '@/composables/usePagination'
import { formatMoney } from '@/utils/format'
import { JOB_AUDIT_STATE_MAP } from '@/utils/enums'
import { success } from '@/utils/message'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import DictTag from '@/components/DictTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import AuditDialog from '@/components/AuditDialog.vue'

const { query, list, total, loading, fetchList, handlePageChange, handleSizeChange, handleSearch } =
  usePagination(adminJobPage, { auditState: 'pending', keyword: '', current: 1, size: 10 })

const auditVisible = ref(false)
const detailVisible = ref(false)
const current = ref(null)

function openAudit(row) {
  current.value = row
  auditVisible.value = true
}

function showDetail(row) {
  current.value = row
  detailVisible.value = true
}

async function handleAudit(form) {
  if (!current.value) return
  await auditJob({
    jobId: current.value.jobId,
    pass: form.pass,
    opinion: form.opinion,
  })
  success(form.pass ? '审核通过' : '已驳回')
  auditVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  text-align: right;
}
.content-pre {
  margin: 0 0 16px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #606266;
  font-family: inherit;
}
</style>
