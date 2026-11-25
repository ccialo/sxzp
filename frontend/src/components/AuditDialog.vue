<template>
  <el-dialog v-model="visible" :title="title" width="480px" @close="handleClose">
    <el-form :model="form" label-width="100px">
      <el-form-item label="审核结论">
        <el-radio-group v-model="form.pass">
          <el-radio :label="true">通过</el-radio>
          <el-radio :label="false">驳回</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="!form.pass" label="驳回意见" required>
        <el-input v-model="form.opinion" type="textarea" :rows="3" placeholder="请输入驳回意见" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '审核' },
})
const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = ref(props.modelValue)
const submitting = ref(false)
const form = ref({
  pass: true,
  opinion: '',
})

watch(() => props.modelValue, (v) => {
  visible.value = v
  if (v) {
    form.value = { pass: true, opinion: '' }
  }
})

watch(visible, (v) => emit('update:modelValue', v))

function handleClose() {
  visible.value = false
}

async function handleConfirm() {
  if (!form.value.pass && !form.value.opinion?.trim()) {
    ElMessage.warning('请输入驳回意见')
    return
  }
  submitting.value = true
  try {
    await emit('confirm', { ...form.value })
    visible.value = false
  } finally {
    submitting.value = false
  }
}
</script>
