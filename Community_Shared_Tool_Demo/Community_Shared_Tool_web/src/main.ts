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
axios.defaults.headers.post['Content-Type'] = 'application/json';

const app = createApp(App)

// 🔹 使用 Element Plus
app.use(ElementPlus)

app.use(createPinia())
app.use(router)

app.mount('#app')