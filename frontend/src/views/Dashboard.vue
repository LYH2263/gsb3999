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

        <h3 class="text-lg font-semibold text-gray-800 mt-6 mb-4">🌱 农事档案（只读）</h3>
        <el-timeline v-if="traceResult.farmingRecords && traceResult.farmingRecords.length > 0">
          <el-timeline-item
            v-for="(rec, idx) in traceResult.farmingRecords"
            :key="idx"
            :timestamp="rec.operationDate"
            :color="farmingTypeMeta(rec.operationType).color"
          >
            <el-tag :type="farmingTypeMeta(rec.operationType).tag" size="small" class="mr-2">{{ farmingTypeMeta(rec.operationType).label }}</el-tag>
            <span v-if="rec.materialName" class="font-bold text-gray-700">{{ rec.materialName }}</span>
            <span v-if="rec.operationType === 'PESTICIDE'" class="ml-2">
              <el-tag type="danger" size="small" effect="plain">安全间隔 {{ rec.safetyIntervalDays }} 天</el-tag>
            </span>
            <div v-if="rec.remark" class="text-sm text-gray-500 mt-1">{{ rec.remark }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无农事档案" />
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
import { publicApi } from '@/api'
import { ElMessage } from 'element-plus'

const traceCode = ref('')
const tracing = ref(false)
const traceResult = ref(null)

const hotProducts = ref([])
const hotLoading = ref(true)

const FARMING_TYPE_META = {
  SOWING: { label: '播种', tag: 'success', color: '#10b981' },
  FERTILIZING: { label: '施肥', tag: 'warning', color: '#f59e0b' },
  PESTICIDE: { label: '用药', tag: 'danger', color: '#ef4444' }
}
const farmingTypeMeta = (t) => FARMING_TYPE_META[t] || { label: t || '未知', tag: 'info', color: '#909399' }

const getHotProducts = async () => {
  try {
    const res = await publicApi.getHotProducts()
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
    const res = await publicApi.trace(traceCode.value.trim())
    traceResult.value = res.data
  } catch(e) {
  } finally {
    tracing.value = false
  }
}

onMounted(() => {
  getHotProducts()
})
</script>
