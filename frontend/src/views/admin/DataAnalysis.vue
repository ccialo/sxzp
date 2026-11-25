<template>
  <div v-loading="loading" class="data-analysis">
    <!-- 页头 -->
    <div class="page-head">
      <div class="head-left">
        <h2>数据分析中心</h2>
        <p>数据更新时间:{{ updateTime }} &nbsp;|&nbsp; 统计口径:全部已提交记录</p>
      </div>
      <div class="head-right">
        <el-radio-group v-model="trendDays" size="default" @change="loadTrend">
          <el-radio-button :value="7">近 7 天</el-radio-button>
          <el-radio-button :value="14">近 14 天</el-radio-button>
          <el-radio-button :value="30">近 30 天</el-radio-button>
        </el-radio-group>
        <el-button :icon="Refresh" @click="loadAll">刷新</el-button>
        <el-button type="primary" :icon="Download" @click="exportCsv">导出报表</el-button>
      </div>
    </div>

    <!-- KPI -->
    <div class="kpis">
      <div v-for="k in kpis" :key="k.label" class="kpi" :style="{ '--kpi-color': k.color }">
        <div class="kpi-icon">
          <el-icon :size="22"><component :is="k.icon" /></el-icon>
        </div>
        <div class="kpi-meta">
          <div class="kpi-label">{{ k.label }}</div>
          <div class="kpi-value">{{ fmt(k.value) }}</div>
          <div class="kpi-sub">{{ k.sub }}</div>
        </div>
      </div>
    </div>

    <!-- 投递趋势 + 投递状态 -->
    <div class="row row-trend">
      <el-card shadow="never" class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar"></span>投递趋势</div>
          <div class="card-sub">
            区间累计 {{ fmt(trendSum) }} 条 · 日均 {{ trendAvg }} 条
          </div>
        </div>
        <ChartBase v-if="trendSum > 0" :option="trendOption" height="280px" />
        <el-empty v-else description="该时间范围内暂无投递数据" :image-size="70" />
      </el-card>
      <el-card shadow="never" class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-warning"></span>投递状态分布</div>
        </div>
        <div class="chart-wrap">
          <div class="donut-center">
            <b>{{ fmt(summary.applyCount) }}</b>
            <span>投递总量</span>
          </div>
          <ChartBase :option="applyStateOption" height="280px" />
        </div>
      </el-card>
    </div>

    <!-- 面试状态 + 热门岗位 -->
    <div class="row row-mid">
      <el-card shadow="never" class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-purple"></span>面试状态分布</div>
        </div>
        <div class="chart-wrap">
          <div class="donut-center">
            <b>{{ fmt(summary.interviewCount) }}</b>
            <span>面试场次</span>
          </div>
          <ChartBase :option="interviewStateOption" height="270px" />
        </div>
      </el-card>
      <el-card shadow="never" class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-teal"></span>热门岗位</div>
          <div class="card-sub">
            展示前
            <el-select
              v-model="rankingLimit"
              size="small"
              style="width: 72px; margin: 0 4px"
              @change="loadRanking"
            >
              <el-option v-for="n in [10, 20, 30, 50]" :key="n" :label="n" :value="n" />
            </el-select>
            条
          </div>
        </div>
        <ChartBase :option="rankingOption" height="320px" />
      </el-card>
    </div>

    <!-- 岗位类型 + 学院分布 + 企业认证 -->
    <div class="row row-tri">
      <el-card shadow="never" class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar"></span>岗位类型分布</div>
        </div>
        <div class="chart-wrap">
          <div class="donut-center">
            <b>{{ fmt(summary.jobCount) }}</b>
            <span>岗位总数</span>
          </div>
          <ChartBase :option="jobTypeOption" height="250px" />
        </div>
      </el-card>
      <el-card shadow="never" class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-success"></span>学生学院分布</div>
          <div class="card-sub">按在校学生数</div>
        </div>
        <ChartBase :option="collegeOption" height="250px" />
      </el-card>
      <el-card shadow="never" class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-danger"></span>企业认证状态</div>
        </div>
        <div class="chart-wrap">
          <div class="donut-center">
            <b>{{ fmt(summary.companyCount) }}</b>
            <span>企业总数</span>
          </div>
          <ChartBase :option="companyAuthOption" height="250px" />
        </div>
      </el-card>
    </div>

    <!-- 学生投递成功率 -->
    <el-card shadow="never" class="card row-single">
      <div class="card-head">
        <div class="card-title"><span class="bar bar-success"></span>学生投递成功率</div>
        <div class="card-sub">成功 = 进入面试及之后环节(面试 / 待协议 / 已录用)</div>
      </div>
      <div class="success-wrap">
        <div class="success-left">
          <div class="donut-box">
            <div class="donut-center">
              <b>{{ Number(successData.rate || 0).toFixed(1) }}%</b>
              <span>投递成功率</span>
            </div>
            <ChartBase :option="successDonutOption" height="220px" />
          </div>
          <div class="success-stats">
            <div class="s-stat">
              <span>总投递</span><b>{{ fmt(successData.total) }}</b>
            </div>
            <div class="s-stat">
              <span>成功投递</span><b>{{ fmt(successData.success) }}</b>
            </div>
            <div class="s-stat">
              <span>未通过</span><b>{{ fmt(stateCount('rejected')) }}</b>
            </div>
            <div class="s-stat">
              <span>待处理 / 已撤回</span>
              <b>{{ fmt(stateCount('pending') + stateCount('withdrawn')) }}</b>
            </div>
          </div>
        </div>
        <div class="success-right">
          <div class="sub-title">各岗位类型投递成功率</div>
          <ChartBase :option="successBarOption" height="180px" />
        </div>
      </div>
      <div class="success-legend">
        <span v-for="item in successStates" :key="item.name" class="legend-item">
          <i class="legend-dot" :style="{ background: item.color }"></i>
          <span class="legend-name">{{ item.name }}</span>
          <b class="legend-val">{{ fmt(item.value) }}</b>
          <span class="legend-pct">{{ pct(item.value, successData.total) }}</span>
        </span>
      </div>
    </el-card>

    <!-- 明细数据(原「热门岗位 / 投递趋势」页面内容,已合并) -->
    <div class="row row-detail">
      <el-card shadow="never" class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar"></span>投递趋势明细</div>
          <div class="card-sub">近 {{ trendDays }} 天</div>
        </div>
        <el-table :data="trendTable" border size="small" max-height="320">
          <el-table-column type="index" label="序号" width="64" />
          <el-table-column label="日期" min-width="120">
            <template #default="{ row }">{{ dayjs(row.date).format('YYYY-MM-DD') }}</template>
          </el-table-column>
          <el-table-column prop="count" label="投递数" width="110" sortable />
          <template #empty>
            <el-empty description="暂无投递数据" :image-size="60" />
          </template>
        </el-table>
      </el-card>
      <el-card shadow="never" class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-teal"></span>热门岗位明细</div>
          <div class="card-sub">按投递量排序 · 前 {{ rankingLimit }} 条</div>
        </div>
        <el-table :data="ranking" border size="small" max-height="320">
          <el-table-column type="index" label="排名" width="64" />
          <el-table-column prop="jobTitle" label="岗位" min-width="150" />
          <el-table-column prop="companyName" label="企业" min-width="140" />
          <el-table-column prop="applyCount" label="投递数" width="90" sortable />
          <el-table-column prop="headcount" label="招聘数" width="90" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="JOB_AUDIT_STATE_MAP[row.auditState]?.type || 'info'" size="small">
                {{ JOB_AUDIT_STATE_MAP[row.auditState]?.label || row.auditState || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无岗位数据" :image-size="60" />
          </template>
        </el-table>
      </el-card>
    </div>

    <!-- 最近投递动态 -->
    <el-card shadow="never" class="card row-single">
      <div class="card-head">
        <div class="card-title"><span class="bar bar-gray"></span>最近投递动态</div>
        <div class="card-sub">最新 {{ recent.length }} 条</div>
      </div>
      <el-table :data="recent" border stripe style="width: 100%">
        <el-table-column prop="studentName" label="学生" min-width="90" />
        <el-table-column prop="studentNo" label="学号" width="110" />
        <el-table-column prop="college" label="学院" min-width="120" />
        <el-table-column prop="jobTitle" label="投递岗位" min-width="170" />
        <el-table-column prop="companyName" label="企业" min-width="170" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="tagType(row.applyState)" size="small">
              {{ stateLabel(row.applyState) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="投递时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.applyTime) }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无投递记录" :image-size="80" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Download } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import ChartBase from '@/components/ChartBase.vue'
import {
  dashboard,
  jobRanking,
  applyTrend,
  jobTypeDistribution,
  collegeDistribution,
  companyAuthDistribution,
  applySuccess,
  recentApplies,
} from '@/api/stats'
import { useDictStore } from '@/stores/dict'
import {
  APPLY_STATE_MAP,
  INTERVIEW_STATE_MAP,
  COMPANY_AUTH_STATE_MAP,
  JOB_AUDIT_STATE_MAP,
} from '@/utils/enums'
import { formatDateTime } from '@/utils/format'

const dictStore = useDictStore()

const loading = ref(false)
const trendDays = ref(14)
const updateTime = ref('')

const summary = ref({})
const trend = ref([])
const ranking = ref([])
const rankingLimit = ref(10)
const jobTypes = ref([])
const colleges = ref([])
const companyAuth = ref([])
const successData = ref({ total: 0, success: 0, rate: 0, jobTypeRates: [] })
const recent = ref([])
const jobTypeDict = ref([])

const fmt = (n) => {
  const num = Number(n || 0)
  return num.toLocaleString('zh-CN')
}

/* ---------------- 数据加载 ---------------- */
async function loadAll() {
  loading.value = true
  try {
    const [s, r, t, jt, c, ca, su, re] = await Promise.all([
      dashboard(),
      jobRanking({ limit: rankingLimit.value }),
      applyTrend({ days: trendDays.value }),
      jobTypeDistribution(),
      collegeDistribution({ limit: 10 }),
      companyAuthDistribution(),
      applySuccess(),
      recentApplies({ limit: 8 }),
    ])
    summary.value = s || {}
    ranking.value = r || []
    trend.value = t || []
    jobTypes.value = jt || []
    colleges.value = c || []
    companyAuth.value = ca || []
    successData.value = su || { total: 0, success: 0, rate: 0, jobTypeRates: [] }
    recent.value = re || []
    updateTime.value = dayjs().format('YYYY-MM-DD HH:mm')
  } catch (e) {
    // 错误提示由 request 拦截器统一处理
  } finally {
    loading.value = false
  }
}

async function loadTrend() {
  try {
    trend.value = (await applyTrend({ days: trendDays.value })) || []
  } catch (e) {
    // 忽略,拦截器已提示
  }
}

async function loadRanking() {
  try {
    ranking.value = (await jobRanking({ limit: rankingLimit.value })) || []
  } catch (e) {
    // 忽略,拦截器已提示
  }
}

async function loadDict() {
  try {
    jobTypeDict.value = (await dictStore.load('job_type')) || []
  } catch (e) {
    jobTypeDict.value = []
  }
}

/* ---------------- 工具 ---------------- */
const jobTypeLabel = (code) => {
  const item = jobTypeDict.value.find((d) => d.dictCode === code)
  return item ? item.dictLabel : code || '未知'
}
const stateLabel = (state) => APPLY_STATE_MAP[state]?.label || state || '-'
const tagType = (state) => APPLY_STATE_MAP[state]?.type || 'info'
const stateCount = (state) => Number(summary.value.applyStateCount?.[state] || 0)
const pct = (part, total) => {
  const t = Number(total || 0)
  if (!t) return '0%'
  return ((Number(part || 0) / t) * 100).toFixed(1) + '%'
}

/* ---------------- KPI ---------------- */
const kpis = computed(() => [
  {
    label: '学生总数',
    value: summary.value.studentCount,
    sub: '平台注册学生',
    color: '#2f6fed',
    icon: 'User',
  },
  {
    label: '企业总数',
    value: summary.value.companyCount,
    sub: `已认证 ${fmt(summary.value.companyAuthedCount)} 家`,
    color: '#8b5cf6',
    icon: 'OfficeBuilding',
  },
  {
    label: '岗位总数',
    value: summary.value.jobCount,
    sub: `在线中 ${fmt(summary.value.jobOnlineCount)} 个`,
    color: '#13c2c2',
    icon: 'Briefcase',
  },
  {
    label: '投递总数',
    value: summary.value.applyCount,
    sub: `今日新增 +${fmt(summary.value.todayApplyCount)}`,
    color: '#faad14',
    icon: 'Position',
  },
  {
    label: '面试安排',
    value: summary.value.interviewCount,
    sub: '累计面试场次',
    color: '#2f6fed',
    icon: 'Calendar',
  },
  {
    label: '实习协议',
    value: summary.value.agreementCount,
    sub: `已签署 ${fmt(summary.value.agreementSignedCount)} 份`,
    color: '#18a058',
    icon: 'Files',
  },
])

/* ---------------- 趋势 ---------------- */
const trendSum = computed(() => trend.value.reduce((s, t) => s + Number(t.count || 0), 0))
const trendAvg = computed(() =>
  trend.value.length ? Math.round(trendSum.value / trend.value.length) : 0
)

const trendTable = computed(() =>
  [...trend.value].sort((a, b) => (a.date > b.date ? 1 : -1))
)

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 48, right: 18, top: 24, bottom: 28 },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trend.value.map((t) => dayjs(t.date).format('MM/DD')),
    axisLine: { lineStyle: { color: '#e4e7ed' } },
    axisTick: { show: false },
    axisLabel: { color: '#909399', fontSize: 11 },
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#eef1f6' } },
    axisLabel: { color: '#909399', fontSize: 11 },
  },
  series: [
    {
      name: '投递量',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 5,
      showSymbol: true,
      data: trend.value.map((t) => Number(t.count || 0)),
      lineStyle: { width: 2.5, color: '#2f6fed' },
      itemStyle: { color: '#2f6fed', borderColor: '#fff', borderWidth: 1.5 },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(47,111,237,0.25)' },
            { offset: 1, color: 'rgba(47,111,237,0)' },
          ],
        },
      },
    },
  ],
}))

/* ---------------- 环形图通用 ---------------- */
function donutOption(items) {
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      bottom: 0,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { color: '#606266', fontSize: 12 },
    },
    series: [
      {
        type: 'pie',
        radius: ['46%', '68%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: false,
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { scale: true, scaleSize: 4 },
        data: items,
      },
    ],
  }
}

const APPLY_COLORS = {
  pending: '#909399',
  interview: '#faad14',
  rejected: '#f56c6c',
  agreement: '#2f6fed',
  hired: '#18a058',
  withdrawn: '#d3dce6',
}
const APPLY_ORDER = ['pending', 'interview', 'rejected', 'agreement', 'hired', 'withdrawn']

const applyStateItems = computed(() => {
  const map = summary.value.applyStateCount || {}
  return APPLY_ORDER.filter((k) => map[k] != null).map((k) => ({
    name: APPLY_STATE_MAP[k]?.label || k,
    value: Number(map[k] || 0),
    itemStyle: { color: APPLY_COLORS[k] || '#2f6fed' },
  }))
})

const applyStateOption = computed(() => donutOption(applyStateItems.value))

const INTERVIEW_COLORS = {
  pending: '#909399',
  confirmed: '#faad14',
  cancelled: '#f56c6c',
  done: '#18a058',
}
const INTERVIEW_ORDER = ['pending', 'confirmed', 'cancelled', 'done']

const interviewStateOption = computed(() => {
  const map = summary.value.interviewStateCount || {}
  const items = INTERVIEW_ORDER.filter((k) => map[k] != null).map((k) => ({
    name: INTERVIEW_STATE_MAP[k]?.label || k,
    value: Number(map[k] || 0),
    itemStyle: { color: INTERVIEW_COLORS[k] || '#2f6fed' },
  }))
  return donutOption(items)
})

const JOB_TYPE_COLORS = ['#2f6fed', '#13c2c2', '#8b5cf6', '#faad14', '#f56c6c', '#18a058']

const jobTypeOption = computed(() => {
  const items = jobTypes.value.map((it, i) => ({
    name: jobTypeLabel(it.name),
    value: Number(it.value || 0),
    itemStyle: { color: JOB_TYPE_COLORS[i % JOB_TYPE_COLORS.length] },
  }))
  return donutOption(items)
})

const COMPANY_COLORS = {
  auth: '#18a058',
  pending: '#909399',
  reject: '#f56c6c',
  freeze: '#faad14',
}

const companyAuthOption = computed(() => {
  const items = companyAuth.value.map((it) => ({
    name: COMPANY_AUTH_STATE_MAP[it.name]?.label || it.name || '未知',
    value: Number(it.value || 0),
    itemStyle: { color: COMPANY_COLORS[it.name] || '#2f6fed' },
  }))
  return donutOption(items)
})

/* ---------------- 条形图通用 ---------------- */
function hbarOption(items, opts = {}) {
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: opts.left || 150, right: opts.right || 60, top: 10, bottom: 10 },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eef1f6' } },
      axisLabel: { color: '#909399', fontSize: 11 },
    },
    yAxis: {
      type: 'category',
      inverse: true,
      data: items.map((i) => i.name),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: '#606266',
        fontSize: 12,
        width: (opts.left || 150) - 12,
        overflow: 'truncate',
      },
    },
    series: [
      {
        type: 'bar',
        barWidth: opts.barWidth || 10,
        data: items.map((i) => i.value),
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: opts.startColor || '#4d86f7' },
              { offset: 1, color: opts.endColor || '#2f6fed' },
            ],
          },
        },
        label: {
          show: true,
          position: 'right',
          color: '#606266',
          fontSize: 12,
          formatter: opts.valueFormatter ? (p) => opts.valueFormatter(p.value) : '{c}',
        },
      },
    ],
  }
}

const rankingOption = computed(() =>
  hbarOption(
    ranking.value.map((r) => ({ name: r.jobTitle || '-', value: Number(r.applyCount || 0) })),
    { left: 160, startColor: '#4d86f7', endColor: '#2f6fed' }
  )
)

const collegeOption = computed(() =>
  hbarOption(
    colleges.value.map((c) => ({ name: c.name || '未填写', value: Number(c.value || 0) })),
    { left: 120, startColor: '#6ea1f9', endColor: '#2f6fed' }
  )
)

/* ---------------- 成功率 ---------------- */
const successStates = computed(() => [
  { name: '成功投递', value: successData.value.success || 0, color: '#18a058' },
  { name: '未通过', value: stateCount('rejected'), color: '#f56c6c' },
  { name: '待处理', value: stateCount('pending'), color: '#909399' },
  { name: '已撤回', value: stateCount('withdrawn'), color: '#d3dce6' },
])

const successDonutOption = computed(() =>
  donutOption(
    successStates.value.map((s) => ({
      name: s.name,
      value: s.value,
      itemStyle: { color: s.color },
    }))
  )
)

const successBarOption = computed(() => {
  const rates = [...(successData.value.jobTypeRates || [])].sort((a, b) => b.rate - a.rate)
  return hbarOption(
    rates.map((r) => ({ name: jobTypeLabel(r.jobType), value: Number(r.rate || 0) })),
    {
      left: 110,
      right: 70,
      barWidth: 12,
      valueFormatter: (v) => `${Number(v).toFixed(1)}%`,
    }
  )
})

/* ---------------- 导出 CSV ---------------- */
function exportCsv() {
  const rows = []
  rows.push(['数据分析报表', updateTime.value])
  rows.push([])
  rows.push(['指标', '数值'])
  kpis.value.forEach((k) => rows.push([k.label, k.value]))
  rows.push([])
  rows.push(['日期', '投递量'])
  trend.value.forEach((t) => rows.push([dayjs(t.date).format('YYYY-MM-DD'), t.count]))
  rows.push([])
  rows.push(['岗位类型', '岗位数'])
  jobTypes.value.forEach((it) => rows.push([jobTypeLabel(it.name), it.value]))
  rows.push([])
  rows.push(['学院', '学生数'])
  colleges.value.forEach((c) => rows.push([c.name || '未填写', c.value]))
  rows.push([])
  rows.push(['企业认证状态', '企业数'])
  companyAuth.value.forEach((c) =>
    rows.push([COMPANY_AUTH_STATE_MAP[c.name]?.label || c.name, c.value])
  )
  rows.push([])
  rows.push(['岗位类型', '投递总数', '成功数', '成功率'])
  ;(successData.value.jobTypeRates || []).forEach((r) =>
    rows.push([jobTypeLabel(r.jobType), r.total, r.success, `${Number(r.rate).toFixed(1)}%`])
  )
  rows.push([])
  rows.push(['学生', '学号', '学院', '投递岗位', '企业', '状态', '投递时间'])
  recent.value.forEach((r) =>
    rows.push([
      r.studentName,
      r.studentNo,
      r.college,
      r.jobTitle,
      r.companyName,
      stateLabel(r.applyState),
      formatDateTime(r.applyTime),
    ])
  )

  const csv =
    '\uFEFF' +
    rows
      .map((row) =>
        row.map((cell) => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(',')
      )
      .join('\r\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `数据分析_${dayjs().format('YYYYMMDD_HHmm')}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('报表已导出')
}

/* ---------------- 初始化 ---------------- */
onMounted(async () => {
  await Promise.all([loadAll(), loadDict()])
})
</script>

<style scoped>
.data-analysis {
  padding-bottom: 8px;
}

/* 页头 */
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.head-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2329;
}
.head-left p {
  margin: 6px 0 0;
  font-size: 12px;
  color: #909399;
}
.head-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* KPI */
.kpis {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.kpi {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04), 0 4px 16px rgba(16, 24, 40, 0.06);
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  position: relative;
  overflow: hidden;
}
.kpi::after {
  content: '';
  position: absolute;
  right: -18px;
  top: -18px;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  opacity: 0.08;
  background: var(--kpi-color, #2f6fed);
}
.kpi-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--kpi-color, #2f6fed);
  background: #f5f8ff;
  flex: none;
}
.kpi-label {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}
.kpi-value {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.5px;
  margin: 2px 0;
  line-height: 1.2;
  color: #1f2329;
}
.kpi-sub {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

/* 布局行 */
.row {
  display: grid;
  gap: 16px;
  margin-bottom: 16px;
}
.row-trend {
  grid-template-columns: 2.1fr 1fr;
}
.row-mid {
  grid-template-columns: 1fr 2.1fr;
}
.row-tri {
  grid-template-columns: repeat(3, 1fr);
}
.row-single {
  margin-bottom: 16px;
}
.row-detail {
  grid-template-columns: 1fr 1.6fr;
}
.card :deep(.el-card__body) {
  padding: 18px 20px;
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1f2329;
}
.card-title .bar {
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: #2f6fed;
}
.bar-warning {
  background: #faad14;
}
.bar-purple {
  background: #8b5cf6;
}
.bar-teal {
  background: #13c2c2;
}
.bar-success {
  background: #18a058;
}
.bar-danger {
  background: #f56c6c;
}
.bar-gray {
  background: #909399;
}
.card-sub {
  font-size: 12px;
  color: #909399;
}

/* 成功率卡片 */
.success-wrap {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 28px;
  align-items: center;
  padding-top: 8px;
}
.success-left {
  display: flex;
  align-items: center;
  gap: 18px;
}
.chart-wrap {
  position: relative;
}
.donut-box {
  position: relative;
  width: 240px;
  flex: none;
}
.donut-center {
  position: absolute;
  left: 50%;
  top: 44%;
  transform: translate(-50%, -11px);
  text-align: center;
  pointer-events: none;
  line-height: 1.2;
  z-index: 1;
}
.donut-center b {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #1f2329;
  font-variant-numeric: tabular-nums;
}
.donut-center span {
  display: block;
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
  white-space: nowrap;
}
.success-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 132px;
}
.s-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.s-stat span {
  font-size: 12px;
  color: #909399;
}
.s-stat b {
  font-size: 18px;
  color: #1f2329;
  font-variant-numeric: tabular-nums;
}
.success-right {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.sub-title {
  font-size: 12.5px;
  color: #909399;
  margin-bottom: 6px;
}
.success-legend {
  display: flex;
  gap: 18px 28px;
  flex-wrap: wrap;
  margin-top: 12px;
  padding-top: 14px;
  border-top: 1px dashed #ebeef5;
}
.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: #606266;
}
.legend-dot {
  width: 9px;
  height: 9px;
  border-radius: 3px;
  display: inline-block;
}
.legend-name {
  white-space: nowrap;
}
.legend-val {
  font-weight: 600;
  color: #1f2329;
  font-variant-numeric: tabular-nums;
}
.legend-pct {
  color: #909399;
  font-variant-numeric: tabular-nums;
}

/* 响应式 */
@media (max-width: 1200px) {
  .kpis {
    grid-template-columns: repeat(3, 1fr);
  }
  .row-trend,
  .row-mid,
  .row-tri,
  .row-detail {
    grid-template-columns: 1fr;
  }
  .success-wrap {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 640px) {
  .kpis {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
