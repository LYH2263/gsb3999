<template>
  <div class="bg-white p-6 rounded-2xl shadow-sm min-h-full flex flex-col">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">
        🌱 {{ isAdmin ? '全部农事档案（只读）' : '我的农事档案' }}
      </h2>
      <el-button v-if="!isAdmin" type="primary" @click="openDialog" round>+ 登记农事记录</el-button>
    </div>

    <el-table :data="records" class="w-full flex-1" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="产品" min-width="160">
        <template #default="{ row }">{{ productNameMap[row.productId] || ('产品#' + row.productId) }}</template>
      </el-table-column>
      <el-table-column label="操作类型" width="110">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.operationType)" effect="dark">{{ typeLabel(row.operationType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operationDate" label="操作日期" width="120" />
      <el-table-column label="用药名称" min-width="140">
        <template #default="{ row }">{{ row.pesticideName || '—' }}</template>
      </el-table-column>
      <el-table-column label="安全间隔(天)" width="110">
        <template #default="{ row }">{{ row.safetyIntervalDays != null ? row.safetyIntervalDays : '—' }}</template>
      </el-table-column>
      <el-table-column label="安全间隔期状态" width="180">
        <template #default="{ row }">
          <span v-if="row.operationType === 'PESTICIDE' && row.safetyIntervalDays != null">
            <el-tag v-if="isInInterval(row)" type="danger" size="small">间隔期内（至 {{ safeUntil(row) }}）</el-tag>
            <el-tag v-else type="success" size="small">已过间隔期</el-tag>
          </span>
          <span v-else class="text-gray-400">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120">
        <template #default="{ row }">{{ row.remark || '—' }}</template>
      </el-table-column>
      <el-table-column v-if="!isAdmin" label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" plain @click="removeRecord(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="登记农事记录" width="520px">
      <el-form label-position="top">
        <el-form-item label="选择农产品">
          <el-select v-model="form.productId" placeholder="请选择自己的产品" style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="p.productName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-radio-group v-model="form.operationType">
            <el-radio label="SOWING">播种</el-radio>
            <el-radio label="FERTILIZING">施肥</el-radio>
            <el-radio label="PESTICIDE">用药</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="操作日期">
          <el-date-picker v-model="form.operationDate" type="date" value-format="YYYY-MM-DD" style="width:100%" :disabled-date="disableFuture" />
        </el-form-item>
        <template v-if="form.operationType === 'PESTICIDE'">
          <el-form-item label="用药名称">
            <el-input v-model="form.pesticideName" placeholder="如：多菌灵、吡虫啉" />
          </el-form-item>
          <el-form-item label="安全间隔天数">
            <el-input-number v-model="form.safetyIntervalDays" :min="0" :max="365" />
          </el-form-item>
        </template>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">确定保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/api'
import { createFarmingRecord, listMyRecords, listAllRecords, deleteRecord } from '@/api/farming'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.role === 'SYS_ADMIN')

const records = ref([])
const products = ref([])
const productNameMap = ref({})
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({ operationType: 'SOWING', safetyIntervalDays: 7 })

const typeLabel = (t) => ({ SOWING: '播种', FERTILIZING: '施肥', PESTICIDE: '用药' }[t] || t)
const typeTag = (t) => ({ SOWING: 'primary', FERTILIZING: 'warning', PESTICIDE: 'danger' }[t] || '')

const parseDate = (d) => new Date(d + 'T00:00:00')
const safeUntil = (row) => {
  const d = parseDate(row.operationDate)
  d.setDate(d.getDate() + (row.safetyIntervalDays || 0))
  return d.toISOString().slice(0, 10)
}
const isInInterval = (row) => {
  return new Date().toISOString().slice(0, 10) <= safeUntil(row)
}
const disableFuture = (date) => date.getTime() > Date.now()

const buildProductMap = () => {
  const map = {}
  products.value.forEach(p => { map[p.id] = p.productName })
  records.value.forEach(r => {
    if (!map[r.productId]) map[r.productId] = '产品#' + r.productId
  })
  productNameMap.value = map
}

const loadData = async () => {
  loading.value = true
  try {
    if (isAdmin.value) {
      const res = await listAllRecords()
      records.value = res.data
    } else {
      const [recRes, prodRes] = await Promise.all([
        listMyRecords(),
        api.get('/farmer/products')
      ])
      records.value = recRes.data
      products.value = prodRes.data
    }
    buildProductMap()
  } finally {
    loading.value = false
  }
}

const openDialog = () => {
  form.value = { operationType: 'SOWING', safetyIntervalDays: 7 }
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.value.productId) return ElMessage.warning('请选择农产品')
  if (!form.value.operationDate) return ElMessage.warning('请选择操作日期')
  submitting.value = true
  try {
    await createFarmingRecord(form.value)
    ElMessage.success('农事记录登记成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const removeRecord = async (id) => {
  await ElMessageBox.confirm('确定删除该条农事记录吗？', '提示', { type: 'warning' })
  await deleteRecord(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>
