<template>
  <div class="weekly-list">
    <PageHeader title="我的周报" desc="回顾实习周报与企业评价">
      <template #actions>
        <el-button type="primary" @click="goSubmit">
          <el-icon><Plus /></el-icon>提交周报
        </el-button>
      </template>
    </PageHeader>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="list" border>
        <el-table-column label="周次" width="80">
          <template #default="{ row }">第 {{ row.weekNum }} 周</template>
        </el-table-column>
        <el-table-column label="本周时间" min-width="200">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }} ~ {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="jobTitle" label="岗位" min-width="140" />
        <el-table-column prop="companyName" label="企业" min-width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StateBadge :state="row.state" :map="WEEKLY_STATE_MAP" />
          </template>
        </el-table-column>
        <el-table-column label="企业评分" width="140">
          <template #default="{ row }">
            <el-rate v-if="row.reviewScore" v-model="row.reviewScore" disabled />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && !list.length" text="还没有提交过周报" />
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

    <el-dialog v-model="detailVisible" title="周报详情" width="640px">
      <template v-if="current">
        <h3>第 {{ current.weekNum }} 周（{{ formatDate(current.startDate) }} ~ {{ formatDate(current.endDate) }}）</h3>
        <h4>本周工作内容</h4>
        <pre class="content-pre">{{ current.content || '暂无' }}</pre>
        <h4>下周计划</h4>
        <pre class="content-pre">{{ current.plan || '暂无' }}</pre>
        <h4>遇到的问题</h4>
        <pre class="content-pre">{{ current.problem || '暂无' }}</pre>
        <template v-if="current.state === 'reviewed'">
          <h4>企业评价</h4>
          <el-rate v-model="current.reviewScore" disabled />
          <pre class="content-pre">{{ current.reviewComment || '暂无评语' }}</pre>
        </template>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { myWeekly, weeklyDetail } from '@/api/weekly'
import { usePagination } from '@/composables/usePagination'
import { formatDate } from '@/utils/format'
import { WEEKLY_STATE_MAP } from '@/utils/enums'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()

const { query, list, total, loading, fetchList, handlePageChange, handleSizeChange } =
  usePagination(myWeekly, { current: 1, size: 10 })

const detailVisible = ref(false)
const current = ref(null)

function goSubmit() {
  router.push('/student/weekly/submit')
}

async function showDetail(row) {
  try {
    current.value = await weeklyDetail(row.reportId)
  } catch {
    current.value = row
  }
  detailVisible.value = true
}

onMounted(fetchList)
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  text-align: right;
}
h3 {
  margin: 0 0 12px;
}
h4 {
  margin: 12px 0 6px;
  color: #409eff;
}
.content-pre {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #606266;
  font-family: inherit;
}
</style>
