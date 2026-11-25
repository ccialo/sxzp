<template>
  <div class="weekly-submit">
    <PageHeader title="提交周报" desc="记录本周实习工作内容与企业评价">
      <template #actions>
        <el-button @click="$router.back()">返回</el-button>
      </template>
    </PageHeader>

    <el-card v-loading="loading" shadow="never">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="default">
        <el-form-item label="关联投递" prop="applyId">
          <el-select v-model="form.applyId" placeholder="选择已通过的投递" style="width: 100%" @change="onApplyChange">
            <el-option
              v-for="a in applyList"
              :key="a.applyId"
              :label="`${a.jobTitle} - ${a.companyName}`"
              :value="a.applyId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="周次" prop="weekNum">
          <el-input-number v-model="form.weekNum" :min="1" :max="52" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="本周开始" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="本周结束" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择结束日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="本周内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入本周工作内容" />
        </el-form-item>
        <el-form-item label="下周计划" prop="plan">
          <el-input v-model="form.plan" type="textarea" :rows="4" placeholder="请输入下周工作计划" />
        </el-form-item>
        <el-form-item label="遇到的问题" prop="problem">
          <el-input v-model="form.problem" type="textarea" :rows="4" placeholder="遇到的问题与解决方案" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSubmit">提交周报</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { submitWeekly } from '@/api/weekly'
import { myApplies } from '@/api/apply'
import { success } from '@/utils/message'
import PageHeader from '@/components/PageHeader.vue'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const applyList = ref([])

const form = reactive({
  applyId: route.query.applyId ? Number(route.query.applyId) : null,
  weekNum: 1,
  startDate: '',
  endDate: '',
  content: '',
  plan: '',
  problem: '',
})

const rules = {
  applyId: [{ required: true, message: '请选择投递记录', trigger: 'change' }],
  weekNum: [{ required: true, message: '请输入周次', trigger: 'blur' }],
}

function onApplyChange() {
  // 预设时间：默认本周一到周日
  const today = new Date()
  const day = today.getDay() || 7
  const monday = new Date(today)
  monday.setDate(today.getDate() - day + 1)
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)
  if (!form.startDate) {
    form.startDate = monday.toISOString().slice(0, 10)
  }
  if (!form.endDate) {
    form.endDate = sunday.toISOString().slice(0, 10)
  }
}

async function loadApplies() {
  loading.value = true
  try {
    const data = await myApplies({ current: 1, size: 100, applyState: 'passed' })
    applyList.value = data?.records || []
    // 没有 passed 的也展示 interview 状态
    if (!applyList.value.length) {
      const data2 = await myApplies({ current: 1, size: 100, applyState: 'interview' })
      applyList.value = data2?.records || []
    }
    if (form.applyId) onApplyChange()
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    await submitWeekly({ ...form })
    success('周报已提交')
    router.push('/student/weekly')
  } finally {
    saving.value = false
  }
}

onMounted(loadApplies)
</script>
