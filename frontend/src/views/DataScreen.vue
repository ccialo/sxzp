<template>
  <div v-loading="loading" class="screen">
    <!-- 头部 -->
    <header class="screen-head">
      <div class="head-left">
        <h1>校园实习招聘数据大屏</h1>
        <p>
          平台实时运营数据 · 更新于 {{ updateTime }}
          <span class="dot">· 每 60 秒自动刷新</span>
        </p>
      </div>
      <div class="head-right">
        <div class="seg">
          <button
            v-for="d in [7, 14, 30]"
            :key="d"
            :class="{ active: trendDays === d }"
            @click="changeDays(d)"
          >
            近 {{ d }} 天
          </button>
        </div>
        <button class="btn" @click="loadAll">刷新</button>
        <router-link to="/" class="btn primary">返回首页</router-link>
      </div>
    </header>

    <div v-if="loadError" class="load-error">
      数据加载失败：后端公开统计接口不可用。请确认后端已重新编译并重启（匿名访问
      <code>/api/public/stats/dashboard</code> 应返回 200）。
    </div>

    <!-- KPI -->
    <section class="kpis">
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
    </section>

    <!-- 投递趋势 + 投递状态 -->
    <section class="row trend">
      <div class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar"></span>投递趋势</div>
          <div class="card-sub">区间累计 {{ fmt(trendSum) }} 条 · 日均 {{ trendAvg }} 条</div>
        </div>
        <ChartBase v-if="trendSum > 0" :option="trendOption" height="300px" />
        <div v-else class="empty-chart">该时间范围内暂无投递数据</div>
      </div>
      <div class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-warning"></span>投递状态分布</div>
        </div>
        <div class="chart-wrap">
          <div class="donut-center">
            <b>{{ fmt(summary.applyCount) }}</b>
            <span>投递总量</span>
          </div>
          <ChartBase :option="applyStateOption" height="300px" />
        </div>
      </div>
    </section>

    <!-- 面试状态 + 热门岗位 -->
    <section class="row mid">
      <div class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-purple"></span>面试状态分布</div>
        </div>
        <div class="chart-wrap">
          <div class="donut-center">
            <b>{{ fmt(summary.interviewCount) }}</b>
            <span>面试场次</span>
          </div>
          <ChartBase :option="interviewStateOption" height="290px" />
        </div>
      </div>
      <div class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-teal"></span>热门岗位 TOP 10</div>
          <div class="card-sub">按投递量排序</div>
        </div>
        <ChartBase :option="rankingOption" height="340px" />
      </div>
    </section>

    <!-- 三列 -->
    <section class="row tri">
      <div class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar"></span>岗位类型分布</div>
        </div>
        <div class="chart-wrap">
          <div class="donut-center">
            <b>{{ fmt(summary.jobCount) }}</b>
            <span>岗位总数</span>
          </div>
          <ChartBase :option="jobTypeOption" height="260px" />
        </div>
      </div>
      <div class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-success"></span>学生学院分布</div>
          <div class="card-sub">按在校学生数</div>
        </div>
        <ChartBase :option="collegeOption" height="260px" />
      </div>
      <div class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-danger"></span>企业认证状态</div>
        </div>
        <div class="chart-wrap">
          <div class="donut-center">
            <b>{{ fmt(summary.companyCount) }}</b>
            <span>企业总数</span>
          </div>
          <ChartBase :option="companyAuthOption" height="260px" />
        </div>
      </div>
    </section>

    <!-- 学生投递成功率 -->
    <section class="row single">
      <div class="card">
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
              <ChartBase :option="successDonutOption" height="230px" />
            </div>
            <div class="success-stats">
              <div class="s-stat"><span>总投递</span><b>{{ fmt(successData.total) }}</b></div>
              <div class="s-stat"><span>成功投递</span><b>{{ fmt(successData.success) }}</b></div>
              <div class="s-stat"><span>未通过</span><b>{{ fmt(stateCount('rejected')) }}</b></div>
              <div class="s-stat">
                <span>待处理 / 已撤回</span>
                <b>{{ fmt(stateCount('pending') + stateCount('withdrawn')) }}</b>
              </div>
            </div>
          </div>
          <div class="success-right">
            <div class="sub-title">各岗位类型投递成功率</div>
            <ChartBase :option="successBarOption" height="190px" />
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
      </div>
    </section>

    <!-- 最近投递动态 -->
    <section class="row single">
      <div class="card">
        <div class="card-head">
          <div class="card-title"><span class="bar bar-gray"></span>最近投递动态</div>
          <div class="card-sub">最新 {{ recent.length }} 条 · 学生信息已脱敏</div>
        </div>
        <table class="table">
          <thead>
            <tr>
              <th>学生</th>
              <th>学号</th>
              <th>学院</th>
              <th>投递岗位</th>
              <th>企业</th>
              <th>状态</th>
              <th>投递时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in recent" :key="row.applyId">
              <td class="name">{{ row.studentName || '-' }}</td>
              <td>{{ row.studentNo || '-' }}</td>
              <td>{{ row.college || '-' }}</td>
              <td>{{ row.jobTitle || '-' }}</td>
              <td>{{ row.companyName || '-' }}</td>
              <td>
                <span class="tag" :class="tagClass(row.applyState)">
                  <i :style="{ background: tagColor(row.applyState) }"></i>
                  {{ stateLabel(row.applyState) }}
                </span>
              </td>
              <td>{{ formatDateTime(row.applyTime) }}</td>
            </tr>
            <tr v-if="!recent.length">
              <td colspan="7" class="empty-row">暂无投递记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <footer class="footnote">
      数据来源于平台真实业务数据，仅展示统计信息；学生个人信息已脱敏。<br />
      校园实习招聘管理平台 · 数据大屏
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import dayjs from 'dayjs'
import ChartBase from '@/components/ChartBase.vue'
import {
  screenDashboard,
  screenApplyTrend,
  screenJobRanking,
  screenJobTypeDistribution,
  screenCollegeDistribution,
  screenCompanyAuthDistribution,
  screenApplySuccess,
  screenRecentApplies,
} from '@/api/publicStats'
import { useDictStore } from '@/stores/dict'
import {
  APPLY_STATE_MAP,
  INTERVIEW_STATE_MAP,
  COMPANY_AUTH_STATE_MAP,
} from '@/utils/enums'
import { formatDateTime } from '@/utils/format'

const dictStore = useDictStore()

const loading = ref(false)
const loadError = ref(false)
const trendDays = ref(14)
const updateTime = ref('')
const now = ref(dayjs())

const summary = ref({})
const trend = ref([])
const ranking = ref([])
const jobTypes = ref([])
const colleges = ref([])
const companyAuth = ref([])
const successData = ref({ total: 0, success: 0, rate: 0, jobTypeRates: [] })
const recent = ref([])
const jobTypeDict = ref([])

const fmt = (n) => Number(n || 0).toLocaleString('zh-CN')

let refreshTimer = null
let clockTimer = null

/* ---------------- 数据加载 ---------------- */
async function loadAll() {
  loading.value = true
  loadError.value = false
  try {
    const [s, r, t, jt, c, ca, su, re] = await Promise.all([
      screenDashboard(),
      screenJobRanking({ limit: 10 }),
      screenApplyTrend({ days: trendDays.value }),
      screenJobTypeDistribution(),
      screenCollegeDistribution({ limit: 10 }),
      screenCompanyAuthDistribution(),
      screenApplySuccess(),
      screenRecentApplies({ limit: 8 }),
    ])
    summary.value = s || {}
    ranking.value = r || []
    trend.value = t || []
    jobTypes.value = jt || []
    colleges.value = c || []
    companyAuth.value = ca || []
    successData.value = su || { total: 0, success: 0, rate: 0, jobTypeRates: [] }
    recent.value = re || []
    updateTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  } catch (e) {
    // 公开接口异常时不阻塞大屏展示，保留上次数据
    loadError.value = true
  } finally {
    loading.value = false
  }
}

async function changeDays(d) {
  trendDays.value = d
  try {
    trend.value = (await screenApplyTrend({ days: d })) || []
  } catch (e) {
    // 忽略
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
const stateCount = (state) => Number(summary.value.applyStateCount?.[state] || 0)
const pct = (part, total) => {
  const t = Number(total || 0)
  if (!t) return '0%'
  return ((Number(part || 0) / t) * 100).toFixed(1) + '%'
}
const tagClass = (state) => `tag-${state || 'pending'}`
const TAG_COLORS = {
  pending: '#909399',
  interview: '#faad14',
  rejected: '#f56c6c',
  agreement: '#2f6fed',
  hired: '#18a058',
  withdrawn: '#c0c4cc',
}
const tagColor = (state) => TAG_COLORS[state] || '#909399'

/* ---------------- KPI ---------------- */
const kpis = computed(() => [
  { label: '学生总数', value: summary.value.studentCount, sub: '平台注册学生', color: '#2f6fed', icon: 'User' },
  { label: '企业总数', value: summary.value.companyCount, sub: `已认证 ${fmt(summary.value.companyAuthedCount)} 家`, color: '#8b5cf6', icon: 'OfficeBuilding' },
  { label: '岗位总数', value: summary.value.jobCount, sub: `在线中 ${fmt(summary.value.jobOnlineCount)} 个`, color: '#13c2c2', icon: 'Briefcase' },
  { label: '投递总数', value: summary.value.applyCount, sub: `今日新增 +${fmt(summary.value.todayApplyCount)}`, color: '#faad14', icon: 'Position' },
  { label: '面试安排', value: summary.value.interviewCount, sub: '累计面试场次', color: '#2f6fed', icon: 'Calendar' },
  { label: '实习协议', value: summary.value.agreementCount, sub: `已签署 ${fmt(summary.value.agreementSignedCount)} 份`, color: '#18a058', icon: 'Files' },
])

/* ---------------- 趋势 ---------------- */
const trendSum = computed(() => trend.value.reduce((s, t) => s + Number(t.count || 0), 0))
const trendAvg = computed(() =>
  trend.value.length ? Math.round(trendSum.value / trend.value.length) : 0
)

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 48, right: 20, top: 24, bottom: 30 },
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

/* ---------------- 环形图 ---------------- */
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

const applyStateOption = computed(() => {
  const map = summary.value.applyStateCount || {}
  return donutOption(
    APPLY_ORDER.filter((k) => map[k] != null).map((k) => ({
      name: APPLY_STATE_MAP[k]?.label || k,
      value: Number(map[k] || 0),
      itemStyle: { color: APPLY_COLORS[k] || '#2f6fed' },
    }))
  )
})

const INTERVIEW_COLORS = {
  pending: '#909399',
  confirmed: '#faad14',
  cancelled: '#f56c6c',
  done: '#18a058',
}
const INTERVIEW_ORDER = ['pending', 'confirmed', 'cancelled', 'done']

const interviewStateOption = computed(() => {
  const map = summary.value.interviewStateCount || {}
  return donutOption(
    INTERVIEW_ORDER.filter((k) => map[k] != null).map((k) => ({
      name: INTERVIEW_STATE_MAP[k]?.label || k,
      value: Number(map[k] || 0),
      itemStyle: { color: INTERVIEW_COLORS[k] || '#2f6fed' },
    }))
  )
})

const JOB_TYPE_COLORS = ['#2f6fed', '#13c2c2', '#8b5cf6', '#faad14', '#f56c6c', '#18a058']

const jobTypeOption = computed(() =>
  donutOption(
    jobTypes.value.map((it, i) => ({
      name: jobTypeLabel(it.name),
      value: Number(it.value || 0),
      itemStyle: { color: JOB_TYPE_COLORS[i % JOB_TYPE_COLORS.length] },
    }))
  )
)

const COMPANY_COLORS = {
  auth: '#18a058',
  pending: '#909399',
  reject: '#f56c6c',
  freeze: '#faad14',
}

const companyAuthOption = computed(() =>
  donutOption(
    companyAuth.value.map((it) => ({
      name: COMPANY_AUTH_STATE_MAP[it.name]?.label || it.name || '未知',
      value: Number(it.value || 0),
      itemStyle: { color: COMPANY_COLORS[it.name] || '#2f6fed' },
    }))
  )
)

/* ---------------- 条形图 ---------------- */
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
    { left: 170, startColor: '#4d86f7', endColor: '#2f6fed' }
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

/* ---------------- 生命周期 ---------------- */
onMounted(async () => {
  await Promise.all([loadAll(), loadDict()])
  refreshTimer = setInterval(loadAll, 60000)
  clockTimer = setInterval(() => {
    now.value = dayjs()
  }, 1000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  if (clockTimer) clearInterval(clockTimer)
})
</script>

<style scoped>
.screen {
  --bg: #f0f2f5;
  --card: #ffffff;
  --primary: #2f6fed;
  --text: #1f2329;
  --text-2: #606266;
  --text-3: #909399;
  --line: #ebeef5;
  --success: #18a058;
  --warning: #faad14;
  --danger: #f56c6c;
  --purple: #8b5cf6;
  --teal: #13c2c2;
  --gray: #909399;
  --radius: 12px;
  --shadow: 0 1px 2px rgba(16, 24, 40, 0.04), 0 4px 16px rgba(16, 24, 40, 0.06);

  min-height: 100vh;
  background: var(--bg);
  color: var(--text);
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
}

/* 头部 */
.screen-head {
  max-width: 1520px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.head-left h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 0.3px;
}
.head-left p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--text-3);
}
.head-left .dot {
  color: var(--primary);
  font-weight: 700;
}
.head-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.seg {
  display: flex;
  background: #e8ecf3;
  border-radius: 8px;
  padding: 3px;
  gap: 2px;
}
.seg button {
  border: 0;
  background: transparent;
  padding: 7px 16px;
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-2);
  cursor: pointer;
  transition: 0.18s;
}
.seg button.active {
  background: #fff;
  color: var(--primary);
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--text-2);
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  text-decoration: none;
  transition: 0.18s;
}
.btn:hover {
  border-color: #c6d4f0;
  color: var(--primary);
}
.btn.primary {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
}
.btn.primary:hover {
  background: #255fd6;
}

/* KPI */
.kpis {
  max-width: 1520px;
  margin: 0 auto 16px;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}
.kpi {
  background: #fff;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
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
  background: var(--kpi-color, var(--primary));
}
.kpi-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f8ff;
  color: var(--kpi-color, var(--primary));
  flex: none;
}
.kpi-meta {
  min-width: 0;
}
.kpi-label {
  font-size: 12px;
  color: var(--text-3);
  white-space: nowrap;
}
.kpi-value {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.5px;
  margin: 2px 0;
  line-height: 1.2;
}
.kpi-sub {
  font-size: 12px;
  color: var(--text-3);
  white-space: nowrap;
}

/* 行 */
.row {
  max-width: 1520px;
  margin: 0 auto 16px;
  display: grid;
  gap: 16px;
}
.row.trend {
  grid-template-columns: 2.1fr 1fr;
}
.row.mid {
  grid-template-columns: 1fr 2.1fr;
}
.row.tri {
  grid-template-columns: repeat(3, 1fr);
}
.row.single {
  grid-template-columns: 1fr;
}
.card {
  background: var(--card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-title .bar {
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: var(--primary);
}
.bar-warning {
  background: var(--warning);
}
.bar-purple {
  background: var(--purple);
}
.bar-teal {
  background: var(--teal);
}
.bar-success {
  background: var(--success);
}
.bar-danger {
  background: var(--danger);
}
.bar-gray {
  background: var(--gray);
}
.card-sub {
  font-size: 12px;
  color: var(--text-3);
}

/* 环形中心 */
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
  font-size: 19px;
  font-weight: 600;
  color: var(--text);
  font-variant-numeric: tabular-nums;
}
.donut-center span {
  display: block;
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
  white-space: nowrap;
}

.empty-chart {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3);
  font-size: 13px;
}
.load-error {
  max-width: 1520px;
  margin: 0 auto 16px;
  padding: 12px 16px;
  border-radius: 8px;
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  color: #c45656;
  font-size: 13px;
}
.load-error code {
  background: #fde2e2;
  padding: 1px 6px;
  border-radius: 4px;
}

/* 成功率 */
.success-wrap {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 32px;
  align-items: center;
  padding-top: 6px;
}
.success-left {
  display: flex;
  align-items: center;
  gap: 22px;
}
.success-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 130px;
}
.s-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.s-stat span {
  font-size: 12px;
  color: var(--text-3);
}
.s-stat b {
  font-size: 18px;
  color: var(--text);
  font-variant-numeric: tabular-nums;
}
.sub-title {
  font-size: 12.5px;
  color: var(--text-3);
  margin-bottom: 8px;
}
.success-legend {
  display: flex;
  gap: 18px 28px;
  flex-wrap: wrap;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed #ebeef5;
}
.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: var(--text-2);
}
.legend-dot {
  width: 9px;
  height: 9px;
  border-radius: 3px;
}
.legend-name {
  white-space: nowrap;
}
.legend-val {
  font-weight: 600;
  color: var(--text);
  font-variant-numeric: tabular-nums;
}
.legend-pct {
  color: var(--text-3);
  font-variant-numeric: tabular-nums;
}

/* 表格 */
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.table th {
  text-align: left;
  font-weight: 500;
  color: var(--text-3);
  font-size: 12px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--line);
  background: #fafbfc;
  white-space: nowrap;
}
.table td {
  padding: 11px 12px;
  border-bottom: 1px solid #f5f7fa;
  color: var(--text-2);
  white-space: nowrap;
}
.table tr:last-child td {
  border-bottom: 0;
}
.table tr:hover td {
  background: #fafcff;
}
.table .name {
  color: var(--text);
  font-weight: 500;
}
.empty-row {
  text-align: center;
  color: var(--text-3);
  padding: 28px 0;
}
.tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 12px;
}
.tag i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
.tag-pending {
  background: #f4f4f5;
  color: #606266;
}
.tag-interview {
  background: #fdf6e3;
  color: #b88230;
}
.tag-rejected {
  background: #fef0f0;
  color: #c45656;
}
.tag-agreement {
  background: #ecf5ff;
  color: #2f6fed;
}
.tag-hired {
  background: #e8f5ec;
  color: #18a058;
}
.tag-withdrawn {
  background: #f4f4f5;
  color: #909399;
}

.footnote {
  max-width: 1520px;
  margin: 18px auto 0;
  font-size: 12px;
  color: #b0b6bf;
  text-align: center;
  line-height: 1.8;
}

/* 响应式 */
@media (max-width: 1280px) {
  .kpis {
    grid-template-columns: repeat(3, 1fr);
  }
  .row.trend,
  .row.mid,
  .row.tri {
    grid-template-columns: 1fr;
  }
  .success-wrap {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 640px) {
  .screen {
    padding: 14px;
  }
  .kpis {
    grid-template-columns: repeat(2, 1fr);
  }
  .success-wrap {
    gap: 16px;
  }
}
</style>
