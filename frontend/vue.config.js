const { defineConfig } = require('@vue/cli-service')
const path = require('path')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8081,
    open: true,
    client: {
      overlay: {
        errors: true,
        warnings: false,
        // 浏览器 ResizeObserver 循环提示属于无害告警,不弹开发浮层;
        // 其他运行时错误仍然照常显示。
        runtimeErrors: (error) => {
          const message = (error && error.message) || ''
          return (
            message !== 'ResizeObserver loop completed with undelivered notifications.' &&
            message !== 'ResizeObserver loop limit exceeded'
          )
        },
      },
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
      },
    },
  },
})
