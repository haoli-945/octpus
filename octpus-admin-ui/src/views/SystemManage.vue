<template>
  <div>
    <!-- 操作栏 -->
    <el-card style="margin-bottom: 16px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span style="font-weight: 500">系统列表</span>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新增系统
        </el-button>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="systemCode" label="系统编码" width="160" />
        <el-table-column prop="systemName" label="系统名称" width="160" />
        <el-table-column prop="baseUrl" label="访问地址" min-width="240">
          <template #default="{ row }">
            <el-tag type="info" effect="plain" size="small">{{ row.baseUrl }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="权重" width="80" align="center" />
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑系统' : '新增系统'" width="520px">
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="系统编码" prop="systemCode">
          <el-input v-model="form.systemCode" placeholder="如 alipay-trade" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="系统名称" prop="systemName">
          <el-input v-model="form.systemName" placeholder="如 支付宝交易系统" />
        </el-form-item>
        <el-form-item label="访问地址" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="如 http://192.168.1.100:8080/service.do" />
        </el-form-item>
        <el-form-item label="权重">
          <el-input-number v-model="form.weight" :min="1" :max="100" />
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
import { getSystemList, createSystem, updateSystem, deleteSystem, toggleSystemStatus } from '../api'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = ref({
  systemCode: '',
  systemName: '',
  baseUrl: '',
  weight: 1,
  status: 1,
  description: ''
})

const rules = {
  systemCode: [{ required: true, message: '请输入系统编码', trigger: 'blur' }],
  systemName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
  baseUrl: [{ required: true, message: '请输入访问地址', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await getSystemList()
  } catch (e) {
    ElMessage.error('加载失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    form.value = { ...row }
  } else {
    isEdit.value = false
    editId.value = null
    form.value = { systemCode: '', systemName: '', baseUrl: '', weight: 1, status: 1, description: '' }
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
      await updateSystem(editId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await createSystem(form.value)
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
    await deleteSystem(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const handleToggleStatus = async (row) => {
  try {
    await toggleSystemStatus(row.id)
    ElMessage.success('状态已切换')
    loadData()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(loadData)
</script>
