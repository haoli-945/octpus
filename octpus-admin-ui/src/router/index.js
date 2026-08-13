import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '总览', icon: 'DataBoard' }
  },
  {
    path: '/system',
    name: 'SystemManage',
    component: () => import('../views/SystemManage.vue'),
    meta: { title: '系统管理', icon: 'Monitor' }
  },
  {
    path: '/service',
    name: 'ServiceManage',
    component: () => import('../views/ServiceManage.vue'),
    meta: { title: '服务管理', icon: 'Connection' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
