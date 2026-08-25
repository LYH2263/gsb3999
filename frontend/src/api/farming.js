import api from './index'

export function createFarmingRecord(data) {
  return api.post('/farming/records', data)
}

export function listRecordsByProduct(productId) {
  return api.get(`/farming/records/product/${productId}`)
}

export function listMyRecords() {
  return api.get('/farming/records/my')
}

export function listAllRecords() {
  return api.get('/farming/records/all')
}

export function deleteRecord(id) {
  return api.delete(`/farming/records/${id}`)
}
