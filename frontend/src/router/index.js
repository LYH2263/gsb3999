import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'products', name: 'Products', component: () => import('@/views/Products.vue'), meta: { roles: ['FARMER', 'SYS_ADMIN'] } },
      { path: 'logistics', name: 'Logistics', component: () => import('@/views/Logistics.vue'), meta: { roles: ['LOGS_ADMIN', 'SYS_ADMIN'] } },
      { path: 'users', name: 'UserAdmin', component: () => import('@/views/UserAdmin.vue'), meta: { roles: ['SYS_ADMIN'] } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const store = useUserStore()
  if (to.path !== '/login' && to.path !== '/register' && !store.token) {
    next('/login')
  } else if (to.meta.roles && !to.meta.roles.includes(store.role) && store.role !== 'SYS_ADMIN') {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
