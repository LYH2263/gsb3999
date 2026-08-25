<template>
  <div class="space-y-6">
    <div class="bg-white p-8 rounded-2xl shadow-sm border border-gray-100">
      <h2 class="text-2xl font-bold text-gray-800 mb-4">🔍 防伪溯源查询</h2>
      <div class="flex space-x-4">
        <el-input v-model="traceCode" placeholder="请输入溯源码 (如: TRC-AKAPPLE-231015-0001A)" size="large" class="max-w-lg shadow-sm" clearable @keyup.enter="queryTrace" />
        <el-button type="primary" size="large" @click="queryTrace" :loading="tracing">溯源查询</el-button>
      </div>

      <div v-if="traceResult" class="mt-8">
        <el-descriptions title="农产品详情" bordered class="bg-gray-50 p-4 rounded-lg">
          <el-descriptions-item label="名称">{{ traceResult.product.productName }}</el-descriptions-item>
          <el-descriptions-item label="类别">
            <el-tag size="small">{{ traceResult.product.category }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="出产地">{{ traceResult.product.origin }}</el-descriptions-item>
          <el-descriptions-item label="采摘日期">{{ traceResult.product.harvestDate }}</el-descriptions-item>
          <el-descriptions-item label="生成溯源码时间">{{ new Date(traceResult.traceInfo.generatedAt).toLocaleString() }}</el-descriptions-item>
        </el-descriptions>
        
        <h3 class="text-lg font-semibold text-gray-800 mt-6 mb-4">🚚 物流流转路径</h3>
        <el-timeline v-if="traceResult.logistics && traceResult.logistics.length > 0">
          <el-timeline-item
            v-for="(log, idx) in traceResult.logistics"
            :key="idx"
            :timestamp="new Date(log.recordedAt).toLocaleString()"
            color="#10b981"
          >
            {{ log.location }} - <span class="font-bold text-green-700">{{ log.statusDesc }}</span>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无物流信息" />

        <h3 class="text-lg font-semibold text-gray-800 mt-6 mb-4">🌱 农事档案</h3>
        <el-timeline v-if="traceResult.farmingRecords && traceResult.farmingRecords.length > 0">
          <el-timeline-item
            v-for="(rec, idx) in traceResult.farmingRecords"
            :key="idx"
            :timestamp="rec.operationDate"
            :color="farmingTypeColor(rec.operationType)"
          >
            <el-tag :type="farmingTypeTag(rec.operationType)" size="small" effect="dark" class="mr-2">{{ farmingTypeLabel(rec.operationType) }}</el-tag>
            <span v-if="rec.operationType === 'PESTICIDE'">
              用药：<strong>{{ rec.pesticideName }}</strong>，安全间隔期 {{ rec.safetyIntervalDays }} 天
              <el-tag v-if="isInInterval(rec)" type="danger" size="small" class="ml-2">间隔期内</el-tag>
              <el-tag v-else type="success" size="small" class="ml-2">已过间隔期</el-tag>
            </span>
            <span v-else>{{ rec.remark || '无备注' }}</span>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无农事档案记录" :image-size="60" />
      </div>
    </div>

    <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
      <h2 class="text-xl font-bold text-gray-800 mb-4 flex items-center">🔥 热门推荐农产品</h2>
      <el-skeleton :rows="3" animated :loading="hotLoading">
        <template #default>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div v-for="(item, index) in hotProducts" :key="index" class="p-4 border rounded-xl hover:shadow-lg transition flex flex-col">
              <div class="text-lg font-bold text-gray-800">{{item.productName}}</div>
              <div class="text-sm text-gray-500 mt-2">产地: {{item.origin}}</div>
              <div class="mt-4 flex justify-between items-center bg-gray-50 p-2 rounded">
                <el-tag type="warning" effect="dark" round>被查询 {{item.searchCount}} 次</el-tag>
                <el-tag type="success" size="small">{{item.category}}</el-tag>
              </div>
            </div>
          </div>
        </template>
      </el-skeleton>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'
import { ElMessage } from 'element-plus'

const traceCode = ref('')
const tracing = ref(false)
const traceResult = ref(null)

const hotProducts = ref([])
const hotLoading = ref(true)

const getHotProducts = async () => {
  try {
    const res = await api.get('/public/hot')
    hotProducts.value = res.data
  } finally {
    hotLoading.value = false
  }
}

const queryTrace = async () => {
  if (!traceCode.value.trim()) return ElMessage.warning('请输入溯源码')
  tracing.value = true
  traceResult.value = null
  try {
    const res = await api.get(`/public/trace/${traceCode.value.trim()}`)
    traceResult.value = res.data
  } catch(e) {
  } finally {
    tracing.value = false
  }
}

const farmingTypeLabel = (t) => ({ SOWING: '播种', FERTILIZING: '施肥', PESTICIDE: '用药' }[t] || t)
const farmingTypeTag = (t) => ({ SOWING: 'primary', FERTILIZING: 'warning', PESTICIDE: 'danger' }[t] || 'info')
const farmingTypeColor = (t) => ({ SOWING: '#409eff', FERTILIZING: '#e6a23c', PESTICIDE: '#f56c6c' }[t] || '#909399')
const isInInterval = (rec) => {
  if (rec.operationType !== 'PESTICIDE' || rec.safetyIntervalDays == null) return false
  const d = new Date(rec.operationDate + 'T00:00:00')
  d.setDate(d.getDate() + rec.safetyIntervalDays)
  return new Date().toISOString().slice(0, 10) <= d.toISOString().slice(0, 10)
}

onMounted(() => {
  getHotProducts()
})
</script>
