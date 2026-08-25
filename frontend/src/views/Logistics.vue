<template>
  <div class="bg-white p-6 rounded-2xl shadow-sm min-h-full">
    <h2 class="text-xl font-bold text-gray-800 mb-6 flex items-center">📦 物流信息上链记录 (Logistics Entry)</h2>
    <div class="max-w-xl mx-auto mt-10 p-8 border border-gray-100 shadow-md rounded-2xl bg-gray-50">
      <el-form label-position="top">
        <el-form-item label="扫描/输入商品溯源码" required>
          <el-input v-model="form.traceCode" size="large" placeholder="如: TRC-AKAPPLE-231015-0001A" />
        </el-form-item>
        <el-form-item label="当前运作位置" required>
          <el-input v-model="form.location" size="large" placeholder="如: 广州市白云区集散中心" />
        </el-form-item>
        <el-form-item label="当前节点状态" required>
          <el-select v-model="form.statusDesc" size="large" class="w-full" placeholder="请选择状态">
            <el-option label="已揽件" value="已揽件" />
            <el-option label="长途运输中" value="长途运输中" />
            <el-option label="到达分拨中心" value="到达分拨中心" />
            <el-option label="派件中" value="派件中" />
            <el-option label="已签收" value="已签收" />
          </el-select>
        </el-form-item>
        <el-button type="primary" size="large" class="w-full mt-4" @click="submitLogistics" round>
          记录上链
        </el-button>
      </el-form>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import api from '@/api'
import { ElMessage } from 'element-plus'

const form = ref({
  traceCode: '', location: '', statusDesc: ''
})

const submitLogistics = async () => {
  if (!form.value.traceCode || !form.value.location || !form.value.statusDesc) {
    return ElMessage.warning('请填写完整信息')
  }
  await api.post('/logistics/record', form.value)
  ElMessage.success('物流状态更新成功！信息已被记录防篡改。')
  form.value.location = ''
  form.value.statusDesc = ''
}
</script>
