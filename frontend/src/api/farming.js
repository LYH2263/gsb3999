import api from './index'

// 农事档案模块 API 封装（组件内禁止直接 axios，一律走这里）

/** 农户登记一条播种/施肥/用药记录 */
export const createFarmingRecord = (data) => api.post('/farming/records', data)

/** 查看自己名下全部档案（管理员为全部档案） */
export const fetchMyFarmingRecords = () => api.get('/farming/records/my')

/** 按产品查看档案（农户限本人产品，管理员不限） */
export const fetchProductFarmingRecords = (productId) => api.get(`/farming/records/product/${productId}`)

/** 农户名下产品列表（用于登记档案时的产品下拉） */
export const fetchMyProducts = () => api.get('/farmer/products')
