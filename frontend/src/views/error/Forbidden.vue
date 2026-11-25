<template>
  <div class="forbidden">
    <div class="content">
      <div class="code">403</div>
      <h2>无权访问</h2>
      <p>抱歉，你没有权限访问此页面</p>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup></script>

<style scoped>
.forbidden {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.content {
  text-align: center;
}
.code {
  font-size: 120px;
  font-weight: 700;
  color: #e6a23c;
  line-height: 1;
}
h2 {
  margin: 16px 0 8px;
  color: #303133;
}
p {
  margin: 0 0 24px;
  color: #909399;
}
</style>
