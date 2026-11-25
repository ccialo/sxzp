<template>
  <div class="job-edit">
    <PageHeader :title="isEdit ? '修改岗位' : '发布岗位'">
      <template #actions>
        <el-button @click="$router.back()">返回</el-button>
      </template>
    </PageHeader>

    <el-card v-loading="loading" shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        size="default"
        style="max-width: 720px"
      >
        <el-form-item label="岗位名称" prop="jobTitle">
          <el-input v-model="form.jobTitle" placeholder="如：Java 后端开发实习生" />
        </el-form-item>
        <el-form-item label="实习类型" prop="jobType">
          <el-select v-model="form.jobType" placeholder="请选择" clearable style="width: 100%">
            <el-option
              v-for="d in jobTypeList"
              :key="d.dictCode"
              :label="d.dictLabel"
              :value="d.dictCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="拟招人数" prop="headcount">
          <el-input-number v-model="form.headcount" :min="1" :max="999" />
        </el-form-item>
        <el-form-item label="工作地点" prop="workPlace">
          <el-input v-model="form.workPlace" placeholder="如：北京/上海/远程" />
        </el-form-item>
        <el-form-item label="岗位方向" prop="category">
          <el-input v-model="form.category" placeholder="如：研发/产品/运营" />
        </el-form-item>
        <el-form-item label="实习津贴" prop="benefitMoney">
          <el-input-number v-model="form.benefitMoney" :min="0" :precision="2" :step="100" />
          <span class="unit">元/月</span>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择结束日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="岗位职责" prop="duty">
          <el-input v-model="form.duty" type="textarea" :rows="5" placeholder="请输入岗位职责" />
        </el-form-item>
        <el-form-item label="任职要求" prop="req">
          <el-input v-model="form.req" type="textarea" :rows="5" placeholder="请输入任职要求" />
        </el-form-item>
        <el-form-item label="提交审核">
          <el-switch v-model="form.submit" :active-value="true" :inactive-value="false" active-text="保存后提交审核" inactive-text="存为草稿" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { saveJob, jobDetail } from '@/api/job'
import { useDictStore } from '@/stores/dict'
import { success } from '@/utils/message'
import PageHeader from '@/components/PageHeader.vue'

const route = useRoute()
const router = useRouter()
const dictStore = useDictStore()

const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const isEdit = computed(() => !!route.query.id)
const jobTypeList = ref([])

const form = reactive({
  jobId: null,
  jobTitle: '',
  jobType: '',
  headcount: 1,
  workPlace: '',
  category: '',
  benefitMoney: 0,
  startDate: '',
  endDate: '',
  duty: '',
  req: '',
  submit: false,
})

const rules = {
  jobTitle: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  jobType: [{ required: true, message: '请选择实习类型', trigger: 'change' }],
  headcount: [{ required: true, message: '请输入拟招人数', trigger: 'blur' }],
}

async function loadDetail() {
  const id = route.query.id
  if (!id) return
  loading.value = true
  try {
    const data = await jobDetail(id)
    Object.assign(form, {
      jobId: data.jobId,
      jobTitle: data.jobTitle,
      jobType: data.jobType,
      headcount: data.headcount,
      workPlace: data.workPlace,
      category: data.category,
      benefitMoney: data.benefitMoney,
      startDate: data.startDate,
      endDate: data.endDate,
      duty: data.duty,
      req: data.req,
      submit: false,
    })
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    await saveJob({ ...form })
    success(form.submit ? '已提交审核' : '已存为草稿')
    router.push('/company/jobs')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    jobTypeList.value = await dictStore.load('job_type')
  } catch {
    // 忽略
  }
  loadDetail()
})
</script>

<style scoped>
.unit {
  margin-left: 8px;
  color: #909399;
}
</style>
