// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import ToolMap from '../views/ToolMap.vue'
import ToolDetail from '../views/ToolDetail.vue' // 🔹 新增导入
import MyBorrow from '../views/MyBorrow.vue'
import MyPublished from '../views/MyPublished.vue'
import MessageCenter from '../views/MessageCenter.vue'
import CommunityChat from '../views/CommunityChat.vue'
import Profile from '../views/Profile.vue'
import Credit from '../views/Credit.vue'
import MainLayout from '../views/MainLayout.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/tool-map' },
    { path: '/login', name: 'Login', component: Login, meta: { isPublic: true } },
    { path: '/register', name: 'Register', component: Register, meta: { isPublic: true } },
    {
      path: '/main',
      name: 'Main',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '/tool-map', name: 'ToolMap', component: ToolMap },
        { path: '/tool/:id', name: 'ToolDetail', component: ToolDetail }, // 🔹 新增路由
        
        { path: '/my-borrow', name: 'MyBorrow', component: MyBorrow },
        { path: '/my-published', name: 'MyPublished', component: MyPublished },
        { path: '/message-center', name: 'MessageCenter', component: MessageCenter },
        { path: '/community-chat', name: 'CommunityChat', component: CommunityChat },
        { path: '/profile', name: 'Profile', component: Profile },
        { path: '/credit', name: 'Credit', component: Credit }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('userToken')
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else if (to.meta.isPublic && isLoggedIn) {
    next('/tool-map')
  } else {
    next()
  }
})

export default router