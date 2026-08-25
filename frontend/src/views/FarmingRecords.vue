<template>
  <div class="bg-white p-6 rounded-2xl shadow-sm min-h-full flex flex-col">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">🌱 农事档案管理</h2>
      <el-button v-if="userStore.role === 'FARMER'" type="primary" @click="openAddDialog" round>+ 登记农事记录</el-button>
    </div>

    <el-alert
      v-if="userStore.role === 'SYS_ADMIN'"
      type="info"
      :closable="false"
      class="mb-4"
      title="管理员可查看全部农事档案，但不可代农户新增记录"
    />

    <div class="flex items-center mb-4 space-x-3">
      <span class="text-gray-600 text-sm">筛选产品：</span>
      <el-select v-model="selectedProductId" placeholder="全部产品" clearable filterable class="w-80" @change="loadRecords">
        <el-option v-for="p in products" :key="p.id" :label="`${p.productName} (ID:${p.id})`" :value="p.id" />
      </el-select>
      <span class="text-xs text-gray-400">仅可选择{{ userStore.role === 'SYS_ADMIN' ? '全部' : '自己名下' }}产品</span>
    </div>

    <el-table :data="records" class="w-full flex-1" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="所属产品" min-width="180">
        <template #default="{row}">{{ productName(row.productId) }}</template>
      </el-table-column>
      <el-table-column label="操作类型" width="100">
        <template #default="{row}">
          <el-tag :type="typeMeta(row.operationType).type">{{ typeMeta(row.operationType).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operationDate" label="操作日期" width="120" />
      <el-table-column label="用药/肥料名称" min-width="160">
        <template #default="{row}">{{ row.materialName || '—' }}</template>
      </el-table-column>
      <el-table-column label="安全间隔天数" width="120">
        <template #default="{row}">
          <span v-if="row.operationType === 'PESTICIDE'">{{ row.safetyIntervalDays }} 天</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160">
        <template #default="{row}">{{ row.remark || '—' }}</template>
      </el-table-column>
      <el-table-column label="录入时间" width="170">
        <template #default="{row}">{{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '—' }}</template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="登记农事记录" width="520px">
      <el-form label-position="top">
        <el-form-item label="关联农产品">
          <el-select v-model="form.productId" placeholder="请选择自己名下的产品" filterable style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="`${p.productName} (采摘日:${p.harvestDate || '未登记'})`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="form.operationType" placeholder="请选择操作类型" style="width:100%">
            <el-option label="播种" value="SOWING" />
            <el-option label="施肥" value="FERTILIZING" />
            <el-option label="用药" value="PESTICIDE" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作日期">
          <el-date-picker v-model="form.operationDate" type="date" value-format="YYYY-MM-DD" :disabled-date="disableFutureDate" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="form.operationType && form.operationType !== 'SOWING'" :label="form.operationType === 'PESTICIDE' ? '用药名称' : '肥料名称'">
          <el-input v-model="form.materialName" :placeholder="form.operationType === 'PESTICIDE' ? '如: 多菌灵可湿性粉剂' : '如: 有机复合肥'" />
        </el-form-item>
        <el-form-item v-if="form.operationType === 'PESTICIDE'" label="安全间隔天数">
          <el-input-number v-model="form.safetyIntervalDays" :min="0" :max="365" controls-position="right" style="width:100%" />
          <div class="text-xs text-gray-400 mt-1">间隔期内该产品将被禁止生成新溯源码</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" placeholder="选填" />
        </el-form-item>
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
import { farmingApi, farmerApi } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const products = ref([])
const records = ref([])
const loading = ref(false)
const selectedProductId = ref(null)
const dialogVisible = ref(false)
const defaultForm = () => ({ productId: null, operationType: 'SOWING', operationDate: '', materialName: '', safetyIntervalDays: 7, remark: '' })
const form = ref(defaultForm())

const TYPE_META = {
  SOWING: { label: '播种', type: 'success' },
  FERTILIZING: { label: '施肥', type: 'warning' },
  PESTICIDE: { label: '用药', type: 'danger' }
}
const typeMeta = (t) => TYPE_META[t] || { label: t || '未知', type: 'info' }
const productName = (id) => {
  const p = products.value.find(item => item.id === id)
  return p ? p.productName : `产品ID:${id}`
}
const disableFutureDate = (date) => date.getTime() > Date.now()

const loadProducts = async () => {
  const res = await farmerApi.getProducts()
  products.value = res.data
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await farmingApi.getRecords(selectedProductId.value)
    records.value = res.data
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  form.value = defaultForm()
  form.value.productId = selectedProductId.value
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.productId) return ElMessage.warning('请选择关联农产品')
  if (!form.value.operationType) return ElMessage.warning('请选择操作类型')
  if (!form.value.operationDate) return ElMessage.warning('请选择操作日期')
  if (form.value.operationType !== 'SOWING' && !form.value.materialName?.trim()) {
    return ElMessage.warning(form.value.operationType === 'PESTICIDE' ? '请填写用药名称' : '请填写肥料名称')
  }
  if (form.value.operationType === 'PESTICIDE' && (form.value.safetyIntervalDays === null || form.value.safetyIntervalDays === undefined)) {
    return ElMessage.warning('请填写安全间隔天数')
  }
  await farmingApi.addRecord({
    productId: form.value.productId,
    operationType: form.value.operationType,
    operationDate: form.value.operationDate,
    materialName: form.value.operationType === 'SOWING' ? null : form.value.materialName,
    safetyIntervalDays: form.value.operationType === 'PESTICIDE' ? form.value.safetyIntervalDays : null,
    remark: form.value.remark
  })
  ElMessage.success('农事记录录入成功')
  dialogVisible.value = false
  selectedProductId.value = form.value.productId
  loadRecords()
}

onMounted(async () => {
  await loadProducts()
  loadRecords()
})
</script>
