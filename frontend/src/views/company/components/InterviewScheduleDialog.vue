<template>
  <el-dialog v-model="visible" title="安排面试" width="500px" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="投递ID">
        <span>{{ applyId }}</span>
      </el-form-item>
      <el-form-item label="面试时间" prop="interviewTime">
        <el-date-picker
          v-model="form.interviewTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="选择面试时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="面试地点" prop="place">
        <el-input v-model="form.place" placeholder="请输入面试地点" />
      </el-form-item>
      <el-form-item label="联系人" prop="contact">
        <el-input v-model="form.contact" placeholder="请输入联系人" />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone">
        <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="备注信息（可选）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { scheduleInterview } from '@/api/interview'
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
  interviewTime: '',
  place: '',
  contact: '',
  contactPhone: '',
  remark: '',
})

const rules = {
  interviewTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }],
}

watch(() => props.modelValue, (v) => {
  visible.value = v
  if (v) {
    Object.assign(form, {
      applyId: props.applyId,
      interviewTime: '',
      place: '',
      contact: '',
      contactPhone: '',
      remark: '',
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
    await scheduleInterview({ ...form })
    success('面试已安排')
    emit('success')
    visible.value = false
  } finally {
    saving.value = false
  }
}
</script>
