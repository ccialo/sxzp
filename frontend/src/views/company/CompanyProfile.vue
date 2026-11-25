<template>
  <div class="company-profile">
    <PageHeader title="企业档案" desc="完善资料以通过认证后即可发布岗位">
      <template #actions>
        <el-button @click="$router.push('/company/jobs')">前往岗位管理</el-button>
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
        <el-form-item label="认证状态">
          <StateBadge v-if="form.authState" :state="form.authState" :map="COMPANY_AUTH_STATE_MAP" />
          <span v-if="form.rejectReason" class="reject-reason">驳回理由：{{ form.rejectReason }}</span>
        </el-form-item>
        <el-form-item label="单位名称" prop="entName">
          <el-input v-model="form.entName" placeholder="请输入单位全称" />
        </el-form-item>
        <el-form-item label="信用代码" prop="creditCode">
          <el-input v-model="form.creditCode" placeholder="统一社会信用代码" />
        </el-form-item>
        <el-form-item label="单位性质" prop="category">
          <el-select v-model="form.category" placeholder="请选择" clearable style="width: 100%">
            <el-option
              v-for="d in categoryList"
              :key="d.dictCode"
              :label="d.dictLabel"
              :value="d.dictCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
        <el-form-item label="单位地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入单位地址" />
        </el-form-item>
        <el-form-item label="企业简介" prop="intro">
          <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="请输入企业简介" />
        </el-form-item>
        <el-form-item label="资质文件" prop="certFile">
          <FileUpload v-model="form.certFile" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button @click="loadProfile">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProfile, saveProfile } from '@/api/company'
import { useDictStore } from '@/stores/dict'
import { COMPANY_AUTH_STATE_MAP } from '@/utils/enums'
import { success } from '@/utils/message'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import FileUpload from '@/components/FileUpload.vue'

const dictStore = useDictStore()
const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const categoryList = ref([])

const form = reactive({
  entName: '',
  creditCode: '',
  category: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  intro: '',
  certFile: '',
  authState: '',
  rejectReason: '',
})

const rules = {
  entName: [{ required: true, message: '请输入单位名称', trigger: 'blur' }],
}

async function loadProfile() {
  loading.value = true
  try {
    const data = await getProfile()
    Object.assign(form, data || {})
  } catch {
    // 新企业可能还没档案，404 之类的由拦截器处理
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
    await saveProfile({ ...form })
    success('保存成功，等待审核认证')
    loadProfile()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    categoryList.value = await dictStore.load('company_category')
  } catch {
    // 忽略
  }
  loadProfile()
})
</script>

<style scoped>
.reject-reason {
  margin-left: 12px;
  color: #f56c6c;
  font-size: 12px;
}
</style>
