const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 7000
  },
  chainWebpack: config => {
    config.module
        .rule('worker')
        .test(/\.worker\.js$/)            // 文件名必须要xxx.worker.js
        .use('worker')
        .loader('worker-loader')
  }
})
