<template>
  <div class="resume-preview">
    <h3 class="resume-title">{{ title }}</h3>

    <!-- 学生基本信息（企业端来自投递快照） -->
    <el-descriptions
      v-if="hasBasic"
      :column="2"
      size="small"
      border
      class="basic-info"
    >
      <el-descriptions-item v-if="basic.studentName" label="姓名">
        {{ basic.studentName }}
      </el-descriptions-item>
      <el-descriptions-item v-if="basic.studentNo" label="学号">
        {{ basic.studentNo }}
      </el-descriptions-item>
      <el-descriptions-item v-if="basic.college" label="学院">
        {{ basic.college }}
      </el-descriptions-item>
      <el-descriptions-item v-if="basic.major" label="专业">
        {{ basic.major }}
      </el-descriptions-item>
    </el-descriptions>

    <!-- 教育经历（表格） -->
    <section v-if="eduList.length" class="section">
      <h4>教育经历</h4>
      <el-table :data="eduList" size="small" border class="edu-table">
        <el-table-column prop="school" label="学校" min-width="120" />
        <el-table-column prop="major" label="专业" min-width="100" />
        <el-table-column label="学历" width="90">
          <template #default="{ row }">{{ degreeLabel(row.degree) }}</template>
        </el-table-column>
        <el-table-column label="起止时间" min-width="170">
          <template #default="{ row }">
            {{ row.startDate || '?' }} ~ {{ row.endDate || '至今' }}
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section v-if="skill" class="section">
      <h4>技能与证书</h4>
      <p class="content">{{ skill }}</p>
    </section>

    <section v-if="projectExp" class="section">
      <h4>项目/实践经历</h4>
      <p class="content">{{ projectExp }}</p>
    </section>

    <section v-if="selfEval" class="section">
      <h4>自我评价</h4>
      <p class="content">{{ selfEval }}</p>
    </section>

    <section v-if="cont" class="section">
      <h4>完整简历</h4>
      <pre class="content-pre">{{ cont }}</pre>
    </section>

    <!-- 兜底：快照无法解析时才显示原始文本 -->
    <section v-if="rawFallback" class="section">
      <h4>简历快照</h4>
      <pre class="content-pre">{{ snapshot }}</pre>
    </section>

    <el-empty v-if="!hasContent && !rawFallback" description="暂无简历内容" :image-size="80" />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 直接传入的简历对象（学生端复用时可传完整简历）
  resume: { type: Object, default: () => ({}) },
  // 投递记录里的 resumeSnapshot（JSON 字符串）
  snapshot: { type: String, default: '' },
})

const DEGREE_LABELS = {
  college: '专科',
  bachelor: '本科',
  master: '硕士',
  doctor: '博士',
}

function degreeLabel(code) {
  return DEGREE_LABELS[code] || code || '-'
}

/** 解析快照 JSON，失败返回 null */
const snapObj = computed(() => {
  if (!props.snapshot) return null
  try {
    const parsed = JSON.parse(props.snapshot)
    return parsed && typeof parsed === 'object' ? parsed : null
  } catch {
    return null
  }
})

/** 合并取值：resume 优先，快照兜底 */
function pick(field) {
  return props.resume?.[field] || snapObj.value?.[field] || ''
}

const title = computed(() => pick('title') || '简历')

/** 教育经历：优先 resume.eduJson，其次快照里的 eduJson（均为 JSON 字符串） */
const eduList = computed(() => {
  const raw = props.resume?.eduJson || snapObj.value?.eduJson
  if (!raw) return []
  try {
    const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

const skill = computed(() => pick('skill'))
const projectExp = computed(() => pick('projectExp'))
const selfEval = computed(() => pick('selfEval'))
const cont = computed(() => pick('cont'))

const basic = computed(() => ({
  studentName: snapObj.value?.studentName || props.resume?.studentName || '',
  studentNo: snapObj.value?.studentNo || props.resume?.studentNo || '',
  college: snapObj.value?.college || props.resume?.college || '',
  major: snapObj.value?.major || props.resume?.major || '',
}))

const hasBasic = computed(() => Object.values(basic.value).some(Boolean))

const hasContent = computed(
  () =>
    hasBasic.value ||
    eduList.value.length > 0 ||
    !!(skill.value || projectExp.value || selfEval.value || cont.value)
)

/** 快照解析失败时才回退显示原始 JSON */
const rawFallback = computed(() => !!props.snapshot && !snapObj.value)
</script>

<style scoped>
.resume-preview {
  font-size: 14px;
  color: #303133;
}
.resume-title {
  margin: 0 0 16px;
  font-size: 18px;
}
.basic-info {
  margin-bottom: 16px;
}
.edu-table {
  width: 100%;
}
.section {
  margin-bottom: 16px;
}
.section h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #409eff;
  border-left: 3px solid #409eff;
  padding-left: 8px;
}
.content {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #606266;
}
.content-pre {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #606266;
  font-family: inherit;
}
</style>
