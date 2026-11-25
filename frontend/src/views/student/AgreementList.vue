<template>
  <div class="agreement-list">
    <PageHeader title="我的协议" desc="查看和签署实习协议" />

    <div class="filter-bar">
      <el-radio-group v-model="query.state" @change="handleSearch">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="pending">待签署</el-radio-button>
        <el-radio-button label="signed">已签署</el-radio-button>
        <el-radio-button label="rejected">已拒绝</el-radio-button>
      </el-radio-group>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="jobTitle" label="岗位" min-width="160" />
        <el-table-column prop="companyName" label="企业" min-width="140" />
        <el-table-column label="实习期" min-width="200">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }} ~ {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column label="津贴" width="120">
          <template #default="{ row }">{{ formatMoney(row.salary) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StateBadge :state="row.state" :map="AGREEMENT_STATE_MAP" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showContent(row)">查看内容</el-button>
            <el-button
              v-if="row.state === 'pending'"
              size="small"
              type="success"
              @click="handleSign(row)"
            >
              签署
            </el-button>
            <el-button
              v-if="row.state === 'pending'"
              size="small"
              type="danger"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && !list.length" text="暂无协议记录" />
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

    <el-dialog v-model="contentVisible" title="协议内容" width="640px">
      <pre class="content-pre">{{ current?.content || '暂无内容' }}</pre>
      <template #footer>
        <el-button v-if="current?.state === 'pending'" type="success" @click="handleSign(current)">
          同意并签署
        </el-button>
        <el-button v-if="current?.state === 'pending'" type="danger" @click="handleReject(current)">
          拒绝
        </el-button>
        <el-button @click="contentVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { myAgreements, signAgreement, rejectAgreement } from '@/api/agreement'
import { usePagination } from '@/composables/usePagination'
import { formatDate, formatMoney } from '@/utils/format'
import { AGREEMENT_STATE_MAP } from '@/utils/enums'
import { success } from '@/utils/message'
import { ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import EmptyState from '@/components/EmptyState.vue'

const { query, list, total, loading, fetchList, handlePageChange, handleSizeChange, handleSearch } =
  usePagination(myAgreements, { state: '', current: 1, size: 10 })

const contentVisible = ref(false)
const current = ref(null)

function showContent(row) {
  current.value = row
  contentVisible.value = true
}

async function handleSign(row) {
  try {
    await ElMessageBox.confirm('确定签署该实习协议吗？', '提示', {
      confirmButtonText: '确认签署',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await signAgreement(row.agreementId)
  success('协议已签署')
  contentVisible.value = false
  fetchList()
}

async function handleReject(row) {
  try {
    await ElMessageBox.confirm('确定拒绝该协议吗？此操作不可撤销', '提示', {
      confirmButtonText: '拒绝',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await rejectAgreement(row.agreementId)
  success('已拒绝协议')
  contentVisible.value = false
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
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #303133;
  font-family: inherit;
  max-height: 400px;
  overflow-y: auto;
}
</style>
