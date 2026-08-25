<template>
  <div class="h-screen w-full flex items-center justify-center bg-gradient-to-br from-green-50 to-green-100">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-xl p-8 transform transition-all hover:scale-[1.01]">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-800 tracking-tight">🌾 农产品溯源系统</h1>
        <p class="text-gray-500 mt-2 text-sm">提供透明、可信的全链路农产品溯源</p>
      </div>
      <el-form label-position="top" @keyup.enter="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="success" :loading="loading" class="w-full mt-4" size="large" @click="handleLogin" round>
            安全登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="mt-4 text-center text-sm">
        还没有账号？ <router-link to="/register" class="text-green-600 hover:text-green-700">立即注册</router-link>
      </div>
      <div class="mt-4 text-center text-xs text-gray-400">
        本项目基于高星级工程模板构建
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import api from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const form = ref({ username: '', password: '' })

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await api.post('/auth/login', form.value)
    userStore.setUserInfo(res.data)
    ElMessage.success(`欢迎回来, ${res.data.realName}`)
    router.push('/dashboard')
  } catch(e) {
    // Error caught by interceptor
  } finally {
    loading.value = false
  }
}
</script>
