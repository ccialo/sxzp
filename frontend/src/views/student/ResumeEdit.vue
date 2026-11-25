<template>
  <div class="resume-edit">
    <PageHeader :title="isEdit ? '编辑简历' : '新建简历'">
      <template #actions>
        <el-button @click="$router.back()">返回</el-button>
      </template>
    </PageHeader>

    <el-card v-loading="loading" shadow="never">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="default">
        <el-form-item label="简历名称" prop="title">
          <el-input v-model="form.title" placeholder="如：软件工程-张同学" style="max-width: 400px" />
        </el-form-item>
        <el-form-item label="是否公开">
          <el-switch v-model="form.isPublic" :active-value="1" :inactive-value="0" active-text="允许企业浏览" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" active-text="设为默认投递简历" />
        </el-form-item>

        <el-divider content-position="left">教育经历</el-divider>
        <div v-for="(edu, idx) in eduList" :key="idx" class="edu-item">
          <el-row :gutter="12">
            <el-col :span="6">
              <el-form-item label="学校">
                <el-input v-model="edu.school" placeholder="请输入学校" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="专业">
                <el-input v-model="edu.major" placeholder="请输入专业" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="学历">
                <el-select v-model="edu.degree" placeholder="请选择">
                  <el-option label="专科" value="college" />
                  <el-option label="本科" value="bachelor" />
                  <el-option label="硕士" value="master" />
                  <el-option label="博士" value="doctor" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="3">
              <el-form-item label="开始">
                <el-date-picker v-model="edu.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="3">
              <el-form-item label="结束">
                <el-date-picker v-model="edu.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="2">
              <el-button text type="danger" @click="removeEdu(idx)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-col>
          </el-row>
        </div>
        <el-button text type="primary" @click="addEdu">
          <el-icon><Plus /></el-icon>添加教育经历
        </el-button>

        <el-divider content-position="left">其他信息</el-divider>
        <el-form-item label="技能与证书">
          <el-input v-model="form.skill" type="textarea" :rows="3" placeholder="如：Java / Python / CET-6" />
        </el-form-item>
        <el-form-item label="项目/实践经历">
          <el-input v-model="form.projectExp" type="textarea" :rows="4" placeholder="请输入项目经历" />
        </el-form-item>
        <el-form-item label="自我评价">
          <el-input v-model="form.selfEval" type="textarea" :rows="3" placeholder="请输入自我评价" />
        </el-form-item>
        <el-form-item label="完整简历">
          <el-input v-model="form.cont" type="textarea" :rows="6" placeholder="可粘贴完整简历正文，或上传附件" />
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
import { saveResume, resumeDetail } from '@/api/resume'
import { success } from '@/utils/message'
import PageHeader from '@/components/PageHeader.vue'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const isEdit = computed(() => !!route.query.id)

const form = reactive({
  resumeId: null,
  title: '',
  isPublic: 1,
  isDefault: 0,
  eduJson: '',
  skill: '',
  projectExp: '',
  selfEval: '',
  cont: '',
})

const eduList = ref([])

const rules = {
  title: [{ required: true, message: '请输入简历名称', trigger: 'blur' }],
}

function addEdu() {
  eduList.value.push({ school: '', major: '', degree: '', startDate: '', endDate: '' })
}

function removeEdu(idx) {
  eduList.value.splice(idx, 1)
}

async function loadDetail() {
  const id = route.query.id
  if (!id) return
  loading.value = true
  try {
    const data = await resumeDetail(id)
    Object.assign(form, data)
    if (data.eduJson) {
      try {
        eduList.value = JSON.parse(data.eduJson)
      } catch {
        eduList.value = []
      }
    }
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
    form.eduJson = JSON.stringify(eduList.value)
    await saveResume({ ...form })
    success('保存成功')
    router.push('/student/resumes')
  } finally {
    saving.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.edu-item {
  background: #fafafa;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 8px;
}
</style>
