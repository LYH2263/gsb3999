<template>
  <div class="bg-white p-6 rounded-2xl shadow-sm min-h-full flex flex-col">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">🌱 农事档案</h2>
      <!-- 仅农户可登记；系统管理员只读查看全部档案 -->
      <el-button v-if="isFarmer" type="primary" @click="openDialog" round>+ 登记档案</el-button>
    </div>

    <el-table :data="records" class="w-full flex-1" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="农产品" min-width="140">
        <template #default="{row}">{{ productName(row.productId) }}</template>
      </el-table-column>
      <el-table-column label="操作类型" width="110">
        <template #default="{row}">
          <el-tag :type="typeTag(row.operationType)" effect="light">{{ typeText(row.operationType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operationDate" label="操作日期" width="120" />
      <el-table-column label="用药名称" width="130">
        <template #default="{row}">{{ row.drugName || '—' }}</template>
      </el-table-column>
      <el-table-column label="安全间隔" width="100">
        <template #default="{row}">
          <span v-if="row.safetyIntervalDays != null">{{ row.safetyIntervalDays }} 天</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="间隔期止" width="120">
        <template #default="{row}">
          <span v-if="row.operationType === 'PESTICIDE' && row.safetyIntervalDays != null"
                :class="inSafetyPeriod(row) ? 'text-red-600 font-bold' : 'text-gray-500'">
            {{ safetyEnd(row) }}
          </span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
        <template #default="{row}">{{ row.remark || '—' }}</template>
      </el-table-column>
      <el-table-column label="登记时间" width="170">
        <template #default="{row}">{{ new Date(row.createdAt).toLocaleString() }}</template>
      </el-table-column>
    </el-table>

    <!-- 登记对话框：仅农户可见 -->
    <el-dialog v-model="dialogVisible" title="登记农事档案" width="520px">
      <el-form label-position="top">
        <el-form-item label="关联农产品" required>
          <el-select v-model="form.productId" placeholder="请选择自己的产品" class="w-full">
            <el-option v-for="p in products" :key="p.id" :value="p.id"
                       :label="`${p.productName}（采摘日: ${p.harvestDate || '未填'}）`" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型" required>
          <el-radio-group v-model="form.operationType">
            <el-radio-button value="SOWING">播种</el-radio-button>
            <el-radio-button value="FERTILIZING">施肥</el-radio-button>
            <el-radio-button value="PESTICIDE">用药</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="操作日期" required>
          <el-date-picker v-model="form.operationDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <template v-if="form.operationType === 'PESTICIDE'">
          <el-form-item label="用药名称" required>
            <el-input v-model="form.drugName" placeholder="如: 苦参碱" />
          </el-form-item>
          <el-form-item label="安全间隔天数" required>
            <el-input-number v-model="form.safetyIntervalDays" :min="0" :max="365" class="w-full" />
          </el-form-item>
        </template>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { createFarmingRecord, fetchMyFarmingRecords, fetchMyProducts } from '@/api/farming'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isFarmer = computed(() => userStore.role === 'FARMER')

const records = ref([])
const products = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const form = ref({})

const productName = (id) => products.value.find(p => p.id === id)?.productName || `#${id}`

const typeText = (t) => ({ SOWING: '播种', FERTILIZING: '施肥', PESTICIDE: '用药' }[t] || t)
const typeTag = (t) => ({ SOWING: 'success', FERTILIZING: 'warning', PESTICIDE: 'danger' }[t] || 'info')

const safetyEnd = (row) => {
  const d = new Date(row.operationDate)
  d.setDate(d.getDate() + row.safetyIntervalDays)
  return d.toISOString().slice(0, 10)
}
const inSafetyPeriod = (row) => safetyEnd(row) >= new Date().toISOString().slice(0, 10)

const loadData = async () => {
  loading.value = true
  try {
    const [recordRes, productRes] = await Promise.all([fetchMyFarmingRecords(), fetchMyProducts()])
    records.value = recordRes.data
    products.value = productRes.data
  } finally {
    loading.value = false
  }
}

const openDialog = () => {
  form.value = { operationType: 'SOWING' }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.productId) return ElMessage.warning('请选择关联农产品')
  if (!form.value.operationDate) return ElMessage.warning('请选择操作日期')
  if (form.value.operationType === 'PESTICIDE') {
    if (!form.value.drugName?.trim()) return ElMessage.warning('请填写用药名称')
    if (form.value.safetyIntervalDays == null) return ElMessage.warning('请填写安全间隔天数')
  }
  submitting.value = true
  try {
    await createFarmingRecord(form.value)
    ElMessage.success('农事档案登记成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadData())
</script>
