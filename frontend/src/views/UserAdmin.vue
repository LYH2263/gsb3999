<template>
  <div class="bg-white p-6 rounded-2xl shadow-sm min-h-full flex flex-col">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">👤 系统用户管理</h2>
      <el-button type="primary" round @click="openCreateDialog">+ 新增用户</el-button>
    </div>

    <el-table :data="users" stripe class="w-full flex-1" :loading="loading" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="160" />
      <el-table-column prop="realName" label="姓名/企业" min-width="160" />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column label="角色权限" min-width="220">
        <template #default="{ row }">
          <div class="flex items-center gap-2">
            <el-select v-model="row.pendingRole" size="small" class="w-36" :disabled="isProtectedAdmin(row)">
              <el-option
                v-for="item in roleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-button
              size="small"
              type="primary"
              plain
              :disabled="isProtectedAdmin(row) || row.pendingRole === row.role"
              @click="saveRole(row)"
            >
              保存权限
            </el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'danger'">
            {{ row.enabled === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.enabled === 1 ? 'warning' : 'success'"
            plain
            :disabled="isProtectedAdmin(row)"
            @click="toggleStatus(row)"
          >
            {{ row.enabled === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" plain :disabled="isProtectedAdmin(row)" @click="deleteUser(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增系统用户" width="520px">
      <el-form label-position="top">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="请输入登录用户名" />
        </el-form-item>
        <el-form-item label="初始密码" required>
          <el-input v-model="form.password" type="password" show-password placeholder="请输入初始密码" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role" class="w-full">
            <el-option
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名/企业">
          <el-input v-model="form.realName" placeholder="请输入姓名或企业名称" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createUser">立即创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const creating = ref(false)
const dialogVisible = ref(false)
const users = ref([])
const roleOptions = [
  { value: 'USER', label: '普通用户' },
  { value: 'FARMER', label: '农户' },
  { value: 'LOGS_ADMIN', label: '物流管理员' },
  { value: 'SYS_ADMIN', label: '系统管理员' }
]

const form = ref({
  username: '',
  password: '',
  role: 'USER',
  realName: '',
  phone: ''
})

const formatDate = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

const resetForm = () => {
  form.value = {
    username: '',
    password: '',
    role: 'USER',
    realName: '',
    phone: ''
  }
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/users')
    users.value = (res.data || []).map(item => ({
      ...item,
      pendingRole: item.role
    }))
  } finally {
    loading.value = false
  }
}

const isProtectedAdmin = (row) => {
  return (row?.username || '').trim().toLowerCase() === 'admin'
}

const openCreateDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const createUser = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写用户名和初始密码')
    return
  }
  creating.value = true
  try {
    await api.post('/admin/users', form.value)
    ElMessage.success('用户创建成功')
    dialogVisible.value = false
    await loadUsers()
  } finally {
    creating.value = false
  }
}

const saveRole = async (row) => {
  if (isProtectedAdmin(row)) {
    ElMessage.warning('admin 为系统保留账号，不允许修改')
    return
  }
  if (row.pendingRole === row.role) return
  const res = await api.put(`/admin/users/${row.id}/role`, { role: row.pendingRole })
  row.role = res.data.role
  row.pendingRole = res.data.role
  ElMessage.success('权限更新成功')
}

const toggleStatus = async (row) => {
  if (isProtectedAdmin(row)) {
    ElMessage.warning('admin 为系统保留账号，不允许修改')
    return
  }
  const nextStatus = row.enabled === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户「${row.username}」吗？`, '提示')
    const res = await api.put(`/admin/users/${row.id}/status`, { enabled: nextStatus })
    row.enabled = res.data.enabled
    ElMessage.success(`${actionText}成功`)
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      // api interceptor will show the server-side error message
    }
  }
}

const deleteUser = async (row) => {
  if (isProtectedAdmin(row)) {
    ElMessage.warning('admin 为系统保留账号，不允许修改')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除用户「${row.username}」吗？删除后不可恢复。`, '高风险操作', {
      type: 'warning'
    })
    await api.delete(`/admin/users/${row.id}`)
    ElMessage.success('删除成功')
    await loadUsers()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      // api interceptor will show the server-side error message
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>
