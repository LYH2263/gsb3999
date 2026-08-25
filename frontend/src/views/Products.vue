<template>
  <div class="bg-white p-6 rounded-2xl shadow-sm min-h-full flex flex-col">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">🌽 我的农产品管理</h2>
      <el-button type="primary" @click="dialogVisible = true" round>+ 增加新产品</el-button>
    </div>

    <el-table :data="products" class="w-full flex-1" stripe :loading="loading" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="productName" label="产品名称" />
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{row}"><el-tag>{{row.category}}</el-tag></template>
      </el-table-column>
      <el-table-column prop="origin" label="产地" />
      <el-table-column prop="harvestDate" label="生产/采摘日" width="120" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="success" plain @click="genTraceCode(row.id)">生成溯源码</el-button>
          <el-button size="small" type="danger" plain @click="deleteProduct(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" title="登记新产品" width="500px">
      <el-form label-position="top">
        <el-form-item label="产品名称"><el-input v-model="form.productName" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" placeholder="如: 水果, 蔬菜" /></el-form-item>
        <el-form-item label="详细产地"><el-input v-model="form.origin" /></el-form-item>
        <el-form-item label="采摘日"><el-date-picker v-model="form.harvestDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="描述"><el-input type="textarea" v-model="form.description" /></el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const products = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.get('/farmer/products')
    products.value = res.data
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  await api.post('/farmer/product', form.value)
  ElMessage.success('产品登记成功')
  dialogVisible.value = false
  form.value = {}
  loadData()
}

const genTraceCode = async (id) => {
  const res = await api.post(`/farmer/trace_code/${id}`)
  ElMessageBox.alert(
    `您的溯源码为：<br/><strong class="text-xl text-green-600">${res.data.traceCode}</strong><br/>请打印并粘贴至包装盒上。`,
    '生成溯源码成功',
    { dangerouslyUseHTMLString: true }
  )
}

const deleteProduct = async (id) => {
  await ElMessageBox.confirm('确定要删除该产品及对应所有溯源数据吗?', '警告')
  await api.delete(`/farmer/product/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>
