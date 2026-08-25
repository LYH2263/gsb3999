import api from '@/api'

// 农事档案模块 API 封装：所有请求统一走此处，禁止在 .vue 中直接调用 axios
export const farmingApi = {
  // 农户登记一条农事记录（播种/施肥/用药）
  createRecord(data) {
    return api.post('/farming/records', data)
  },
  // 农户查看自己名下全部农事档案
  getMyRecords() {
    return api.get('/farming/records/my')
  },
  // 按产品查看农事档案（农户仅限自己产品，管理员可查全部）
  getRecordsByProduct(productId) {
    return api.get(`/farming/records/product/${productId}`)
  },
  // 拉取当前用户可操作的农产品列表（用于选择登记对象）
  getProducts() {
    return api.get('/farmer/products')
  }
}

export default farmingApi
