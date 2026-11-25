<template>
  <div class="weekly-received">
    <PageHeader title="收到的周报" desc="查看学生周报并评价" />

    <div class="filter-bar">
      <el-radio-group v-model="query.state" @change="handleSearch">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="submitted">待评价</el-radio-button>
        <el-radio-button label="reviewed">已评价</el-radio-button>
      </el-radio-group>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="studentName" label="学生" min-width="100" />
        <el-table-column prop="jobTitle" label="岗位" min-width="140" />
        <el-table-column label="周次" width="80">
          <template #default="{ row }">第 {{ row.weekNum }} 周</template>
        </el-table-column>
        <el-table-column label="本周时间" min-width="200">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }} ~ {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StateBadge :state="row.state" :map="WEEKLY_STATE_MAP" />
          </template>
        </el-table-column>
        <el-table-column label="评分" width="160">
          <template #default="{ row }">
            <el-rate v-if="row.reviewScore" v-model="row.reviewScore" disabled />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">查看详情</el-button>
            <el-button
              v-if="row.state === 'submitted'"
              size="small"
              type="primary"
              @click="openReview(row)"
            >
              评价
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && !list.length" text="暂无周报" />
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

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="周报详情" width="640px">
      <template v-if="current">
        <h3>第 {{ current.weekNum }} 周（{{ formatDate(current.startDate) }} ~ {{ formatDate(current.endDate) }}）</h3>
        <h4>本周内容</h4>
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
        <el-button v-if="current?.state === 'submitted'" type="primary" @click="openReview(current)">
          评价
        </el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 评价 -->
    <el-dialog v-model="reviewVisible" title="评价周报" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.score" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="4" placeholder="请输入评语" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewing" @click="handleReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { receivedWeekly, weeklyDetail, reviewWeekly } from '@/api/weekly'
import { usePagination } from '@/composables/usePagination'
import { formatDate } from '@/utils/format'
import { WEEKLY_STATE_MAP } from '@/utils/enums'
import { success } from '@/utils/message'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import EmptyState from '@/components/EmptyState.vue'

const { query, list, total, loading, fetchList, handlePageChange, handleSizeChange, handleSearch } =
  usePagination(receivedWeekly, { state: '', current: 1, size: 10 })

const detailVisible = ref(false)
const reviewVisible = ref(false)
const reviewing = ref(false)
const current = ref(null)
const reviewForm = reactive({ reportId: null, score: 5, comment: '' })

async function showDetail(row) {
  try {
    current.value = await weeklyDetail(row.reportId)
  } catch {
    current.value = row
  }
  detailVisible.value = true
}

function openReview(row) {
  reviewForm.reportId = row.reportId
  reviewForm.score = 5
  reviewForm.comment = ''
  reviewVisible.value = true
}

async function handleReview() {
  if (!reviewForm.score) {
    success('请选择评分')
    return
  }
  reviewing.value = true
  try {
    await reviewWeekly({ ...reviewForm })
    success('评价已提交')
    reviewVisible.value = false
    detailVisible.value = false
    fetchList()
  } finally {
    reviewing.value = false
  }
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
