<template>
  <div>
    <!-- 操作栏 -->
    <el-card style="margin-bottom: 16px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div style="display: flex; gap: 12px; align-items: center">
          <span style="font-weight: 500">服务列表</span>
          <el-select v-model="filterSystem" placeholder="按系统筛选" clearable style="width: 180px" @change="loadData">
            <el-option v-for="s in systemOptions" :key="s.systemCode" :label="s.systemName" :value="s.systemCode" />
          </el-select>
        </div>
        <div style="display: flex; gap: 8px">
          <el-button @click="handleFlushCache" :loading="flushing">
            <el-icon><Refresh /></el-icon> 刷新缓存
          </el-button>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon> 新增服务
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="serviceName" label="服务名 (serviceName)" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag effect="plain" size="small" style="font-family: monospace">{{ row.serviceName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="systemCode" label="所属系统" width="140">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.systemCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column prop="timeoutMs" label="超时(ms)" width="90" align="center" />
        <el-table-column prop="retryCount" label="重试" width="70" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="handleToggleStatus(row)"
              inline-prompt
              active-text="上线"
              inactive-text="下线"
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑服务' : '新增服务'" width="560px">
      <el-form :model="form" label-width="90px" :rules="rules" ref="formRef">
        <el-form-item label="服务名" prop="serviceName">
          <el-input v-model="form.serviceName" placeholder="如 open.alipay.trade.pay（全局唯一）"
            :disabled="isEdit" style="font-family: monospace" />
        </el-form-item>
        <el-form-item label="所属系统" prop="systemCode">
          <el-select v-model="form.systemCode" placeholder="选择所属系统" style="width: 100%">
            <el-option v-for="s in systemOptions" :key="s.systemCode" :label="`${s.systemName} (${s.systemCode})`" :value="s.systemCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="form.version" placeholder="默认 1.0" />
        </el-form-item>
        <el-form-item label="超时(ms)">
          <el-input-number v-model="form.timeoutMs" :min="500" :max="60000" :step="500" />
        </el-form-item>
        <el-form-item label="重试次数">
          <el-input-number v-model="form.retryCount" :min="0" :max="5" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0"
            active-text="上线" inactive-text="下线" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getServiceList, createService, updateService, deleteService,
  toggleServiceStatus, flushServiceCache, getSystemList
} from '../api'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const systemOptions = ref([])
const loading = ref(false)
const flushing = ref(false)
const filterSystem = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = ref({
  serviceName: '',
  systemCode: '',
  version: '1.0',
  timeoutMs: 3000,
  retryCount: 0,
  status: 1,
  description: ''
})

const rules = {
  serviceName: [{ required: true, message: '请输入服务名', trigger: 'blur' }],
  systemCode: [{ required: true, message: '请选择所属系统', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await getServiceList(filterSystem.value || undefined)
  } catch (e) {
    ElMessage.error('加载失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

const loadSystems = async () => {
  try {
    systemOptions.value = await getSystemList()
  } catch {}
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    form.value = { ...row }
  } else {
    isEdit.value = false
    editId.value = null
    form.value = { serviceName: '', systemCode: '', version: '1.0', timeoutMs: 3000, retryCount: 0, status: 1, description: '' }
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch { return }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateService(editId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await createService(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await deleteService(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const handleToggleStatus = async (row) => {
  try {
    await toggleServiceStatus(row.id)
    ElMessage.success('状态已切换')
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const handleFlushCache = async () => {
  flushing.value = true
  try {
    await flushServiceCache()
    ElMessage.success('缓存已刷新')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    flushing.value = false
  }
}

onMounted(() => {
  loadSystems()
  loadData()
})
</script>
