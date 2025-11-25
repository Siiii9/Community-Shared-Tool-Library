<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const errorMessage = ref('');
const loading = ref(false);

const router = useRouter();

const handleRegister = async () => {
  errorMessage.value = '';
  loading.value = true;

  if (password.value !== confirmPassword.value) {
    errorMessage.value = '两次密码输入不一致';
    loading.value = false;
    return;
  }

  try {
    const response = await axios.post('/api/user/register', {
      username: username.value,
      password: password.value,
      basicInfo: '' // 无 basicInfo 输入，固定为空字符串
    });

    if (response.data.success) {
      console.log('注册成功');
      router.push('/login');
    } else {
      errorMessage.value = response.data.message || '注册失败';
    }
  } catch (error) {
    if (error.response) {
      errorMessage.value = error.response.data.message || '注册失败';
      console.error('注册失败:', error.response.data);
    } else if (error.request) {
      errorMessage.value = '无法连接后端，请检查服务';
      console.error('无响应:', error.request);
    } else {
      errorMessage.value = '请求配置错误';
      console.error('配置错误:', error.message);
    }
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-container">
    <form class="login-form" @submit.prevent="handleRegister">
      <h2>系统注册</h2>

      <div class="input-group">
        <label for="username">用户名</label>
        <input type="text" id="username" v-model="username" placeholder="请输入用户名" autocomplete="username" required />
      </div>

      <div class="input-group">
        <label for="password">密码</label>
        <input type="password" id="password" v-model="password" placeholder="请输入密码" autocomplete="new-password" required />
      </div>

      <div class="input-group">
        <label for="confirmPassword">确认密码</label>
        <input type="password" id="confirmPassword" v-model="confirmPassword" placeholder="确认密码" autocomplete="new-password" required />
      </div>

      <div v-if="errorMessage" class="error-message">
        <span>{{ errorMessage }}</span>
      </div>

      <button type="submit" class="login-button" :disabled="loading">
        {{ loading ? '注册中...' : '注册' }}
      </button>

      <div class="redirect-login">
        <p>已有账号？<router-link to="/login">登录</router-link></p>
      </div>
    </form>
  </div>
</template>

<style scoped>
/* 页面基本样式 */
html,
body {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
}

/* 登录容器 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(180deg, #f8fafc, #eef5fc);
}

/* 登录表单 */
.login-form {
  background: white;
  padding: 2rem;
  border-radius: 16px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  animation: fadeIn 0.5s ease-in-out;
}

/* 渐入动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 注册标题 */
.login-form h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #34495e;
  font-size: 1.8rem;
  font-weight: bold;
  letter-spacing: -0.02em;
}

/* 输入组 */
.input-group {
  margin-bottom: 1.2rem;
  position: relative;
}

/* 输入标签 */
.input-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 600;
  letter-spacing: 0.02em;
}

/* 输入框 */
.input-group input {
  width: 100%;
  padding: 1rem 1.2rem;
  border: 1px solid #ccd0d5;
  border-radius: 8px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

/* 输入框悬停态 */
.input-group input:focus,
.input-group input:hover {
  border-color: #2c3e50;
  outline: none;
  background: #fafbfc;
}

/* 注册按钮 */
.login-button {
  width: 100%;
  padding: 1.2rem;
  background-image: linear-gradient(45deg, #3498db, #2980b9);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: inset 0 0 0 0 #2775b5;
  position: relative;
  overflow: hidden;
}

/* 注册按钮悬停态 */
.login-button:hover {
  box-shadow: inset 400px 0 0 0 #2775b5;
}

/* 注册按钮激活态 */
.login-button:active {
  transform: translateY(1px);
}

/* 错误信息样式 */
.error-message {
  color: #e74c3c;
  margin-bottom: 1rem;
  text-align: center;
  font-size: 0.9rem;
}

/* 加载状态提示 */
.login-button:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

/* 登录跳转提示 */
.redirect-login {
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.redirect-login a {
  color: #2980b9;
}
</style>
