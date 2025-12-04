// src/stores/userStore.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('userToken') || '')
  const userId = ref(localStorage.getItem('userId') || '1')
  const username = ref(localStorage.getItem('username') || '')
  const isAdmin = ref(localStorage.getItem('isAdmin') === 'true')

  const login = (userData: { token: string; userId: string; username: string; isAdmin: boolean }) => {
    token.value = userData.token
    userId.value = userData.userId
    username.value = userData.username
    isAdmin.value = userData.isAdmin
    
    localStorage.setItem('userToken', userData.token)
    localStorage.setItem('userId', userData.userId)
    localStorage.setItem('username', userData.username)
    localStorage.setItem('isAdmin', userData.isAdmin ? 'true' : 'false')
  }

  const logout = () => {
    token.value = ''
    userId.value = ''
    username.value = ''
    isAdmin.value = false
    
    localStorage.removeItem('userToken')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('isAdmin')
  }

  const setAdmin = (value: boolean) => {
    isAdmin.value = value
    localStorage.setItem('isAdmin', value ? 'true' : 'false')
  }

  return {
    token,
    userId,
    username,
    isAdmin,
    login,
    logout,
    setAdmin
  }
})