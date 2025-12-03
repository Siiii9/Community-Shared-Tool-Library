import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// 🔹 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css' // 引入样式

import axios from 'axios';
axios.defaults.baseURL = 'http://localhost:8084';
axios.defaults.headers.common['Content-Type'] = 'application/json';

// 添加请求拦截器，自动携带token
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('userToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 添加响应拦截器，处理401错误
axios.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 清除本地存储的用户信息
      localStorage.removeItem('userToken');
      localStorage.removeItem('userId');
      localStorage.removeItem('username');
      // 跳转到登录页
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

const app = createApp(App)

// 🔹 使用 Element Plus
app.use(ElementPlus)

app.use(createPinia())
app.use(router)

app.mount('#app')