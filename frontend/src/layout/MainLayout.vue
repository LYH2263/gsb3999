<template>
  <div class="flex h-screen w-full bg-gray-50">
    <aside class="w-64 bg-white shadow-md flex flex-col">
      <div class="h-16 flex items-center justify-center font-bold text-xl text-green-600 border-b">
        🌿 农产品溯源系统
      </div>
      <nav class="flex-1 py-4">
        <router-link to="/dashboard" class="block px-6 py-3 text-gray-700 hover:bg-green-50 hover:text-green-600 transition-colors" active-class="bg-green-50 text-green-600 border-r-4 border-green-600">
          溯源与推荐 (首页)
        </router-link>
        <router-link v-if="['FARMER', 'SYS_ADMIN'].includes(userStore.role)" to="/products" class="block px-6 py-3 text-gray-700 hover:bg-green-50 hover:text-green-600 transition-colors" active-class="bg-green-50 text-green-600 border-r-4 border-green-600">
          农产品管理
        </router-link>
        <router-link v-if="['LOGS_ADMIN', 'SYS_ADMIN'].includes(userStore.role)" to="/logistics" class="block px-6 py-3 text-gray-700 hover:bg-green-50 hover:text-green-600 transition-colors" active-class="bg-green-50 text-green-600 border-r-4 border-green-600">
          物流上链管理
        </router-link>
        <router-link v-if="userStore.role === 'SYS_ADMIN'" to="/users" class="block px-6 py-3 text-gray-700 hover:bg-green-50 hover:text-green-600 transition-colors" active-class="bg-green-50 text-green-600 border-r-4 border-green-600">
          系统用户管理
        </router-link>
      </nav>
      <div class="p-4 border-t flex flex-col">
        <span class="text-sm text-gray-500 mb-2">当前用户: {{userStore.realName}} ({{userStore.role}})</span>
        <el-button type="danger" plain @click="logout" class="w-full">退出登录</el-button>
      </div>
    </aside>
    <main class="flex-1 overflow-y-auto p-8 relative">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>
<script setup>
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>
