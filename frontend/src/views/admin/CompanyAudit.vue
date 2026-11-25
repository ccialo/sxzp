<template>
  <div class="company-audit">
    <PageHeader title="企业审核" desc="审核企业认证申请" />

    <div class="filter-bar">
      <el-radio-group v-model="query.authState" @change="handleSearch">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="pending">待审核</el-radio-button>
        <el-radio-button label="auth">已认证</el-radio-button>
        <el-radio-button label="reject">未通过</el-radio-button>
        <el-radio-button label="freeze">已冻结</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="query.keyword"
        placeholder="企业名称 / 信用代码"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="list" border>
        <el-table-column prop="entName" label="单位名称" min-width="200" />
        <el-table-column prop="creditCode" label="信用代码" width="200" />
        <el-table-column label="单位性质" width="120">
          <template #default="{ row }">
            <DictTag type-code="company_category" :code="row.category" />
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column label="认证状态" width="100">
          <template #default="{ row }">
            <StateBadge :state="row.authState" :map="COMPANY_AUTH_STATE_MAP" />
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回理由" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.authState === 'pending'"
              size="small"
              type="primary"
              @click="openAudit(row)"
            >
              审核
            </el-button>
            <el-button v-else size="small" @click="showDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-if="!loading && !list.length" text="暂无待审核企业" />
    </el-card>

    <div v-if="total > 0" class="pagination">
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <AuditDialog
      v-model="auditVisible"
      title="审核企业认证"
      @confirm="handleAudit"
    />

    <el-dialog v-model="detailVisible" title="企业详情" width="640px">
      <template v-if="current">
        <p><strong>单位名称：</strong>{{ current.entName }}</p>
        <p><strong>信用代码：</strong>{{ current.creditCode }}</p>
        <p><strong>联系人：</strong>{{ current.contactName }}</p>
        <p><strong>联系电话：</strong>{{ current.contactPhone }}</p>
        <p><strong>联系邮箱：</strong>{{ current.contactEmail }}</p>
        <p><strong>单位地址：</strong>{{ current.address }}</p>
        <p><strong>企业简介：</strong></p>
        <pre class="content-pre">{{ current.intro || '暂无' }}</pre>
        <p v-if="current.certFile"><strong>资质文件：</strong></p>
        <FilePreview v-if="current.certFile" :path="current.certFile" />
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pageCompanies, auditCompany } from '@/api/company'
import { usePagination } from '@/composables/usePagination'
import { COMPANY_AUTH_STATE_MAP } from '@/utils/enums'
import { success } from '@/utils/message'
import PageHeader from '@/components/PageHeader.vue'
import StateBadge from '@/components/StateBadge.vue'
import DictTag from '@/components/DictTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import AuditDialog from '@/components/AuditDialog.vue'
import FilePreview from '@/components/FilePreview.vue'

const { query, list, total, loading, fetchList, handlePageChange, handleSizeChange, handleSearch } =
  usePagination(pageCompanies, { authState: 'pending', keyword: '', current: 1, size: 10 })

const auditVisible = ref(false)
const detailVisible = ref(false)
const current = ref(null)

function openAudit(row) {
  current.value = row
  auditVisible.value = true
}

function showDetail(row) {
  current.value = row
  detailVisible.value = true
}

async function handleAudit(form) {
  if (!current.value) return
  await auditCompany({
    companyId: current.value.companyId,
    pass: form.pass,
    opinion: form.opinion,
  })
  success(form.pass ? '审核通过' : '已驳回')
  auditVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  text-align: right;
}
.content-pre {
  margin: 0 0 16px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #606266;
  font-family: inherit;
}
</style>
