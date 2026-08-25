<template>
  <div class="bg-white p-6 rounded-2xl shadow-sm min-h-full flex flex-col">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">🌱 农事档案</h2>
      <!-- 仅农户可见「+ 登记」按钮；管理员为只读 -->
      <el-button v-if="isFarmer" type="primary" @click="openDialog" round>+ 登记农事记录</el-button>
      <el-tag v-else type="info" effect="plain">管理员只读视图，不可代农户新增</el-tag>
    </div>

    <div class="mb-4 flex items-center gap-3">
      <span class="text-gray-600 text-sm">按产品筛选：</span>
      <el-select v-model="filterProductId" placeholder="全部产品" clearable style="width: 280px" @change="loadRecords">
        <el-option v-for="p in products" :key="p.id" :label="`#${p.id} ${p.productName}`" :value="p.id" />
      </el-select>
    </div>

    <el-table :data="records" class="w-full flex-1" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="productId" label="产品ID" width="90" />
      <el-table-column label="操作类型" width="110">
        <template #default="{row}">
          <el-tag :type="opTagType(row.operationType)">{{ opLabel(row.operationType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operationDate" label="操作日期" width="130" />
      <el-table-column prop="drugName" label="用药名称">
        <template #default="{row}">{{ row.drugName || '—' }}</template>
      </el-table-column>
      <el-table-column label="安全间隔(天)" width="120">
        <template #default="{row}">{{ row.safetyIntervalDays != null ? row.safetyIntervalDays : '—' }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注">
        <template #default="{row}">{{ row.remark || '—' }}</template>
      </el-table-column>
    </el-table>

    <!-- 登记对话框：仅农户使用 -->
    <el-dialog v-model="dialogVisible" title="登记农事记录" width="520px">
      <el-form label-position="top">
        <el-form-item label="选择产品">
          <el-select v-model="form.productId" placeholder="请选择自己的农产品" style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="`#${p.id} ${p.productName}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="form.operationType" placeholder="请选择" style="width:100%">
            <el-option label="播种" value="SOWING" />
            <el-option label="施肥" value="FERTILIZING" />
            <el-option label="用药" value="PESTICIDE" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作日期">
          <el-date-picker v-model="form.operationDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <template v-if="form.operationType === 'PESTICIDE'">
          <el-form-item label="用药名称">
            <el-input v-model="form.drugName" placeholder="如: 低毒杀菌剂" />
          </el-form-item>
          <el-form-item label="安全间隔天数">
            <el-input-number v-model="form.safetyIntervalDays" :min="0" style="width:100%" />
          </el-form-item>
        </template>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定登记</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { farmingApi } from '@/api/farming'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isFarmer = computed(() => userStore.role === 'FARMER')

const products = ref([])
const records = ref([])
const loading = ref(false)
const filterProductId = ref('')
const dialogVisible = ref(false)
const form = ref({})

const opLabel = (t) => ({ SOWING: '播种', FERTILIZING: '施肥', PESTICIDE: '用药' }[t] || t)
const opTagType = (t) => ({ SOWING: 'success', FERTILIZING: 'warning', PESTICIDE: 'danger' }[t] || 'info')

const loadProducts = async () => {
  const res = await farmingApi.getProducts()
  products.value = res.data
}

const loadRecords = async () => {
  loading.value = true
  try {
    if (filterProductId.value) {
      const res = await farmingApi.getRecordsByProduct(filterProductId.value)
      records.value = res.data
    } else if (isFarmer.value) {
      const res = await farmingApi.getMyRecords()
      records.value = res.data
    } else {
      // 管理员未选产品时提示先筛选
      records.value = []
    }
  } finally {
    loading.value = false
  }
}

const openDialog = () => {
  form.value = { safetyIntervalDays: 0 }
  dialogVisible.value = true
}

const submitForm = async () => {
  await farmingApi.createRecord(form.value)
  ElMessage.success('农事记录登记成功')
  dialogVisible.value = false
  form.value = {}
  loadRecords()
}

onMounted(async () => {
  await loadProducts()
  loadRecords()
})
</script>
