<template>
  <el-dialog v-model="visible" title="发起实习协议" width="560px" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="投递ID">
        <span>{{ applyId }}</span>
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
      <el-form-item label="实习津贴" prop="salary">
        <el-input-number v-model="form.salary" :min="0" :precision="2" :step="100" />
        <span class="unit">元/月</span>
      </el-form-item>
      <el-form-item label="协议内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入协议内容（可包含双方权利义务等）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleConfirm">发起协议</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { saveAgreement } from '@/api/agreement'
import { success } from '@/utils/message'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  applyId: { type: Number, default: null },
})
const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(props.modelValue)
const formRef = ref()
const saving = ref(false)
const form = reactive({
  applyId: null,
  startDate: '',
  endDate: '',
  salary: 0,
  content: '',
})

const rules = {
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  salary: [{ required: true, message: '请输入津贴', trigger: 'blur' }],
}

watch(() => props.modelValue, (v) => {
  visible.value = v
  if (v) {
    Object.assign(form, {
      applyId: props.applyId,
      startDate: '',
      endDate: '',
      salary: 0,
      content: '',
    })
  }
})

watch(visible, (v) => emit('update:modelValue', v))

function handleClose() {
  visible.value = false
}

async function handleConfirm() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    await saveAgreement({ ...form })
    success('协议已发起')
    emit('success')
    visible.value = false
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.unit {
  margin-left: 8px;
  color: #909399;
}
</style>
