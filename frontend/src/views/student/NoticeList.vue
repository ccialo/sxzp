<template>
  <div class="notice-list">
    <PageHeader title="消息通知" desc="站内消息，实时掌握进展">
      <template #actions>
        <el-button type="primary" @click="handleReadAll">全部已读</el-button>
      </template>
    </PageHeader>

    <div class="filter-bar">
      <el-radio-group v-model="query.type" @change="handleSearch">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="system">系统</el-radio-button>
        <el-radio-button label="apply">投递</el-radio-button>
        <el-radio-button label="interview">面试</el-radio-button>
        <el-radio-button label="agreement">协议</el-radio-button>
      </el-radio-group>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="list" border @row-click="handleRowClick">
        <el-table-column label="" width="50">
          <template #default="{ row }">
            <el-badge v-if="!row.isRead" is-dot />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <StateBadge :state="row.type" :map="NOTICE_TYPE_MAP" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'info' : 'danger'" size="small">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.isRead" size="small" @click.stop="handleRead(row)">标记已读</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && !list.length" text="暂无消息" />
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

    <el-dialog v-model="contentVisible" :title="current?.title" width="500px">
      <p class="content">{{ current?.content }}</p>
      <template #footer>
        <el-button @click="contentVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listNotices, readNotice, readAllNotices } from '@/api/notice'
import { usePagination } from '@/composables/usePagination'
import { formatDateTime } from '@/utils/format'
import { NOTICE_TYPE_MAP } from '@/utils/enums'
import { success } from '@/utils/message'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import EmptyState from '@/components/EmptyState.vue'

const { query, list, total, loading, fetchList, handlePageChange, handleSizeChange, handleSearch } =
  usePagination(listNotices, { type: '', current: 1, size: 10 })

const contentVisible = ref(false)
const current = ref(null)

async function handleRowClick(row) {
  current.value = row
  contentVisible.value = true
  if (!row.isRead) {
    await readNotice(row.noticeId)
    row.isRead = 1
  }
}

async function handleRead(row) {
  await readNotice(row.noticeId)
  row.isRead = 1
  success('已标记已读')
}

async function handleReadAll() {
  await readAllNotices()
  success('已全部标记已读')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  text-align: right;
}
.content {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #303133;
}
</style>
