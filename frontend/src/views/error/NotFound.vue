<template>
  <div class="not-found">
    <div class="content">
      <div class="code">404</div>
      <h2>页面不存在</h2>
      <p>抱歉，你访问的页面找不到了</p>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup></script>

<style scoped>
.not-found {
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
  color: #409eff;
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
