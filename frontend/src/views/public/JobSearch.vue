<template>
  <div class="job-search">
    <PageHeader title="岗位检索" desc="找到适合你的实习机会">
      <template #actions>
        <el-button @click="goBack">
          <el-icon style="margin-right: 4px"><Back /></el-icon>返回
        </el-button>
        <el-button type="primary" @click="goHome">
          <el-icon style="margin-right: 4px"><HomeFilled /></el-icon>主界面
        </el-button>
      </template>
    </PageHeader>

    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="岗位名称 / 企业名称"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.jobType" placeholder="实习类型" clearable style="width: 140px">
        <el-option
          v-for="d in jobTypeList"
          :key="d.dictCode"
          :label="d.dictLabel"
          :value="d.dictCode"
        />
      </el-select>
      <el-input
        v-model="query.workPlace"
        placeholder="工作地点"
        clearable
        style="width: 160px"
      />
      <el-input
        v-model.number="query.minBenefit"
        placeholder="最低津贴"
        clearable
        style="width: 120px"
      />
      <el-select v-model="query.sortBy" placeholder="排序" style="width: 140px">
        <el-option label="最新发布" value="publish_time" />
        <el-option label="津贴高低" value="benefit_money" />
      </el-select>
      <el-switch v-model="query.desc" active-text="降序" inactive-text="升序" />
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <div v-loading="loading" class="job-list">
      <el-row :gutter="16">
        <el-col v-for="job in list" :key="job.jobId" :xs="24" :sm="12" :md="8" :lg="6">
          <JobCard :job="job" class="job-card-item" @click="goDetail(job.jobId)">
            <template #actions>
              <el-button
                v-if="authStore.isLoggedIn && authStore.userType === 'student'"
                size="small"
                :type="job.applied ? 'info' : 'primary'"
                :disabled="job.applied"
                @click.stop="openApplyDialog(job)"
              >
                {{ job.applied ? '已投递' : '立即投递' }}
              </el-button>
            </template>
          </JobCard>
        </el-col>
      </el-row>
      <EmptyState v-if="!loading && !list.length" text="暂无匹配的岗位" />
    </div>

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

    <!-- 投递对话框 -->
    <el-dialog v-model="applyDialog.visible" title="选择简历" width="480px">
      <el-form>
        <el-form-item label="岗位">
          <span>{{ applyDialog.job?.jobTitle }} - {{ applyDialog.job?.companyName }}</span>
        </el-form-item>
        <el-form-item label="简历">
          <el-select v-model="applyDialog.resumeId" placeholder="不选则使用默认简历" clearable>
            <el-option
              v-for="r in resumeList"
              :key="r.resumeId"
              :label="r.title + (r.isDefault ? '（默认）' : '')"
              :value="r.resumeId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="applyDialog.loading" @click="confirmApply">确定投递</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getHomeRoute } from '@/permission'
import { useDictStore } from '@/stores/dict'
import { searchJobs } from '@/api/job'
import { listResumes, setDefault as setDefaultResume } from '@/api/resume'
import { submitApply } from '@/api/apply'
import { usePagination } from '@/composables/usePagination'
import JobCard from '@/components/JobCard.vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { success, error } from '@/utils/message'

const router = useRouter()
const authStore = useAuthStore()
const dictStore = useDictStore()
const jobTypeList = ref([])

const { query, list, total, loading, fetchList, handlePageChange, handleSizeChange, handleSearch } =
  usePagination(searchJobs, {
    keyword: '',
    jobType: '',
    workPlace: '',
    minBenefit: undefined,
    sortBy: 'publish_time',
    desc: true,
    current: 1,
    size: 10,
  })

const resumeList = ref([])
const applyDialog = reactive({
  visible: false,
  loading: false,
  job: null,
  resumeId: null,
})

function goDetail(id) {
  router.push(`/jobs/${id}`)
}

function goBack() {
  // 有浏览历史则返回上一页，否则回主界面兜底
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    router.push('/')
  }
}

function goHome() {
  // 已登录用户回各自角色的工作台，未登录回平台首页
  router.push(authStore.isLoggedIn ? getHomeRoute(authStore.userType) : '/')
}

async function loadResumes() {
  if (authStore.userType !== 'student') return
  try {
    resumeList.value = await listResumes()
  } catch {
    // 静默
  }
}

function openApplyDialog(job) {
  if (!resumeList.value.length) {
    error('请先创建简历')
    router.push('/student/resumes')
    return
  }
  applyDialog.job = job
  applyDialog.resumeId = resumeList.value.find((r) => r.isDefault)?.resumeId || null
  applyDialog.visible = true
}

async function confirmApply() {
  if (!applyDialog.job) return
  applyDialog.loading = true
  try {
    await submitApply({
      jobId: applyDialog.job.jobId,
      resumeId: applyDialog.resumeId,
    })
    success('投递成功')
    applyDialog.job.applied = true
    applyDialog.visible = false
  } catch {
    // 拦截器已弹错误
  } finally {
    applyDialog.loading = false
  }
}

onMounted(async () => {
  try {
    jobTypeList.value = await dictStore.load('job_type')
  } catch {
    // 忽略
  }
  fetchList()
  loadResumes()
})
</script>

<style scoped>
.job-search {
  padding-bottom: 24px;
}
.job-card-item {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
