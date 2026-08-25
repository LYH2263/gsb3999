<template>
  <div class="h-screen w-full flex items-center justify-center bg-gradient-to-br from-green-50 to-green-100">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-xl p-8 transform transition-all hover:scale-[1.01]">
      <div class="text-center mb-6">
        <h1 class="text-3xl font-bold text-gray-800 tracking-tight">🌾 用户注册</h1>
        <p class="text-gray-500 mt-2 text-sm">欢迎加入农产品溯源系统</p>
      </div>
      <el-form label-position="top" @keyup.enter="handleRegister">
        <el-form-item label="身份角色" required>
          <el-select v-model="form.role" class="w-full" size="large" placeholder="请选择您的身份">
            <el-option label="普通查验用户" value="USER" />
            <el-option label="农产品农户" value="FARMER" />
            <el-option label="物流管理员" value="LOGS_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="请输入用于登录的用户名" size="large" />
        </el-form-item>
        <el-form-item label="真实姓名/企业名" required>
          <el-input v-model="form.realName" placeholder="请输入真实姓名或企业名" size="large" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" size="large" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password />
        </el-form-item>
        <el-form-item label="确认密码" required>
          <el-input v-model="confirmPassword" type="password" placeholder="请再次输入密码" size="large" show-password />
        </el-form-item>
        
        <el-form-item class="mt-2">
          <el-button type="success" :loading="loading" class="w-full" size="large" @click="handleRegister" round>
            立即注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="mt-3 text-center text-sm">
        已有账号？ <router-link to="/login" class="text-green-600 hover:text-green-700">返回登录</router-link>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const confirmPassword = ref('')
const form = ref({
  username: '',
  password: '',
  role: 'USER',
  realName: '',
  phone: ''
})

const handleRegister = async () => {
  if (!form.value.username || !form.value.password || !form.value.realName) {
    ElMessage.warning('请填写必填项(用户名, 姓名, 密码)')
    return
  }
  if (form.value.password !== confirmPassword.value) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  loading.value = true
  try {
    await api.post('/auth/register', form.value)
    ElMessage.success('注册成功，请登录！')
    router.push('/login')
  } catch(e) {
    // 拦截器内已包含错误冒泡提示
  } finally {
    loading.value = false
  }
}
</script>
