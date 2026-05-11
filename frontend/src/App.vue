<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 全局主题色与基础背景（主色改回 Element Plus 默认蓝色） */
:root {
  --el-color-primary: #409eff;
  --el-color-primary-dark-2: #337ecc;
  --el-color-primary-light-3: #79bbff;
  --el-color-primary-light-5: #a0cfff;
  --el-color-primary-light-7: #c6e2ff;

  --el-color-success: #4caf50;
  --el-color-success-light-3: #81c784;

  --page-bg-color: #f9f9f9;
  --text-color-main: #333333;
  --text-color-secondary: #666666;
  --divider-color: #eeeeee;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: var(--page-bg-color);
  color: var(--text-color-main);
}
</style>

