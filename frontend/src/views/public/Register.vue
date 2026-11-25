<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <h1>注册账号</h1>
        <p>选择身份并填写信息，开启你的招聘之旅</p>
      </div>
      <el-radio-group v-model="form.userType" size="large" class="role-switch">
        <el-radio-button label="student">学生注册</el-radio-button>
        <el-radio-button label="company">企业注册</el-radio-button>
      </el-radio-group>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="default"
        class="register-form"
      >
        <el-divider content-position="left">账号信息</el-divider>
        <el-form-item label="登录账号" prop="account">
          <el-input v-model="form.account" placeholder="学生用学号，企业用自定义账号" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="6-32 位" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>

        <template v-if="form.userType === 'student'">
          <el-divider content-position="left">学生信息</el-divider>
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="form.studentNo" placeholder="请输入学号" />
          </el-form-item>
          <el-form-item label="学院" prop="college">
            <el-input v-model="form.college" placeholder="请输入学院" />
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-input v-model="form.major" placeholder="请输入专业" />
          </el-form-item>
          <el-form-item label="毕业年份" prop="gradeYear">
            <el-input v-model="form.gradeYear" placeholder="如 2026" />
          </el-form-item>
        </template>

        <template v-else>
          <el-divider content-position="left">企业信息</el-divider>
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
          <el-form-item label="单位地址" prop="address">
            <el-input v-model="form.address" placeholder="请输入单位地址" />
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">
            立即注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        已有账号？<router-link to="/login">直接登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import { getHomeRoute } from '@/permission'
import { success } from '@/utils/message'

const router = useRouter()
const authStore = useAuthStore()
const dictStore = useDictStore()

const formRef = ref()
const loading = ref(false)
const categoryList = ref([])

const form = reactive({
  account: '',
  password: '',
  userType: 'student',
  realName: '',
  email: '',
  phone: '',
  // 学生
  studentNo: '',
  college: '',
  major: '',
  gradeYear: '',
  // 企业
  entName: '',
  creditCode: '',
  category: '',
  address: '',
})

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, max: 40, message: '账号长度 4-40', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  entName: [{ required: true, message: '请输入单位名称', trigger: 'blur' }],
}

watch(
  () => form.userType,
  () => {
    formRef.value?.clearValidate()
  }
)

onMounted(async () => {
  try {
    categoryList.value = await dictStore.load('company_category')
  } catch {
    // 忽略
  }
})

async function handleRegister() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await authStore.register({ ...form })
    success('注册成功')
    router.push(getHomeRoute(authStore.userType))
  } catch {
    // 拦截器已弹错误
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  width: 100%;
  max-width: 640px;
  padding: 20px 0;
  margin: 0 auto;
}
.register-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}
.register-header {
  text-align: center;
  margin-bottom: 16px;
}
.register-header h1 {
  margin: 0 0 4px;
  font-size: 22px;
  color: #303133;
}
.register-header p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
.role-switch {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}
.register-form {
  margin-top: 8px;
}
.submit-btn {
  width: 100%;
}
.register-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
  color: #909399;
}
</style>
