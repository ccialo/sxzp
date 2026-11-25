<template>
  <div class="resume-list">
    <PageHeader title="我的简历" desc="管理多份简历，灵活切换投递">
      <template #actions>
        <el-button type="primary" @click="$router.push('/student/resumes/edit')">
          <el-icon><Plus /></el-icon>新建简历
        </el-button>
      </template>
    </PageHeader>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="list" border style="width: 100%">
        <el-table-column prop="title" label="简历名称" min-width="200" />
        <el-table-column label="是否公开" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isPublic ? 'success' : 'info'" size="small">
              {{ row.isPublic ? '公开' : '私密' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="默认简历" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/student/resumes/edit?id=${row.resumeId}`)">
              编辑
            </el-button>
            <el-button
              v-if="!row.isDefault"
              size="small"
              type="success"
              @click="handleSetDefault(row)"
            >
              设为默认
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && !list.length" text="还没有简历，点击右上角新建一份吧" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listResumes, setDefault, deleteResume } from '@/api/resume'
import { formatDateTime } from '@/utils/format'
import { success } from '@/utils/message'
import { ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await listResumes()
  } finally {
    loading.value = false
  }
}

async function handleSetDefault(row) {
  await setDefault(row.resumeId)
  success('已设为默认')
  load()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除简历「${row.title}」吗？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await deleteResume(row.resumeId)
  success('删除成功')
  load()
}

onMounted(load)
</script>
