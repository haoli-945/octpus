<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside width="220px" style="background: #001529">
      <div style="padding: 20px; text-align: center">
        <span style="color: #fff; font-size: 20px; font-weight: bold">
          🐙 八爪鱼网关
        </span>
        <div style="color: rgba(255,255,255,0.45); font-size: 12px; margin-top: 4px">
          Octpus Admin Console
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#001529"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#fff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>总览</span>
        </el-menu-item>
        <el-menu-item index="/system">
          <el-icon><Monitor /></el-icon>
          <span>系统管理</span>
        </el-menu-item>
        <el-menu-item index="/service">
          <el-icon><Connection /></el-icon>
          <span>服务管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header style="background: #fff; border-bottom: 1px solid #f0f0f0; display: flex; align-items: center; justify-content: space-between">
        <span style="font-size: 16px; font-weight: 500">{{ currentTitle }}</span>
        <el-tag type="success" effect="dark">v1.4.0</el-tag>
      </el-header>
      <el-main style="background: #f5f5f5; padding: 20px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '八爪鱼网关管理')
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
.el-aside .el-menu { border-right: none; }
</style>
