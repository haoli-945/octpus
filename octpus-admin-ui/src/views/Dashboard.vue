<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center">
            <el-icon :size="40" color="#409eff"><Monitor /></el-icon>
            <div style="margin-left: 16px">
              <div style="font-size: 28px; font-weight: bold">{{ stats.totalSystems || 0 }}</div>
              <div style="color: #999; font-size: 14px">注册系统总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center">
            <el-icon :size="40" color="#67c23a"><CircleCheck /></el-icon>
            <div style="margin-left: 16px">
              <div style="font-size: 28px; font-weight: bold; color: #67c23a">{{ stats.onlineSystems || 0 }}</div>
              <div style="color: #999; font-size: 14px">在线系统</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center">
            <el-icon :size="40" color="#e6a23c"><Connection /></el-icon>
            <div style="margin-left: 16px">
              <div style="font-size: 28px; font-weight: bold">{{ stats.totalServices || 0 }}</div>
              <div style="color: #999; font-size: 14px">注册服务总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center">
            <el-icon :size="40" color="#409eff"><Service /></el-icon>
            <div style="margin-left: 16px">
              <div style="font-size: 28px; font-weight: bold; color: #409eff">{{ stats.onlineServices || 0 }}</div>
              <div style="color: #999; font-size: 14px">在线服务</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 各系统服务分布 -->
    <el-card>
      <template #header>
        <span style="font-weight: 500">📊 各系统服务分布</span>
      </template>
      <el-table :data="stats.servicesBySystem || []" stripe style="width: 100%">
        <el-table-column prop="system_code" label="系统编码" />
        <el-table-column prop="system_name" label="系统名称" />
        <el-table-column prop="service_count" label="服务数量" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="warning" effect="dark">{{ row.service_count }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!stats.servicesBySystem?.length" description="暂无数据，请先注册系统和服务" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboardStats } from '../api'
import { ElMessage } from 'element-plus'

const stats = ref({})

onMounted(async () => {
  try {
    stats.value = await getDashboardStats()
  } catch (e) {
    ElMessage.error('加载统计数据失败: ' + e.message)
  }
})
</script>
