<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

// 创建变量用于表单输入
const username = ref('');
const password = ref('');
const errorMessage = ref('');
const loading = ref(false);

const router = useRouter();

// 登录处理函数
const handleLogin = async () => {
    errorMessage.value = '';
    loading.value = true;

    try {
        const response = await axios.post('/api/user/login', {
            username: username.value,
            password: password.value,
        }, { withCredentials: true });

        console.log('Response:', response); // 添加调试日志
        if (response.data.success) {
            console.log('登录成功');
            localStorage.setItem('userToken', response.data.token); // 存储后端返回的 token
            router.push('/main');
        } else {
            errorMessage.value = response.data.message || '登录失败';
        }
    } catch (error) {
        console.error('Error:', error); // 添加错误日志
        if (error.response) {
            errorMessage.value = error.response.data.message || '登录失败';
        } else if (error.request) {
            errorMessage.value = '网络连接异常，请检查后端服务或 CORS 配置';
        } else {
            errorMessage.value = '请求配置错误';
        }
    } finally {
        loading.value = false;
    }
};
// 跳转到注册页面
const redirectToRegister = () => {
  router.push('/register');
};
</script>

<template>
  <div class="login-container">
    <form class="login-form" @submit.prevent="handleLogin">
      <h2>系统登录</h2>

      <div class="input-group">
        <label for="username">用户名</label>
        <input type="text" id="username" v-model="username" placeholder="请输入用户名" required />
      </div>

      <div class="input-group">
        <label for="password">密码</label>
        <input type="password" id="password" v-model="password" placeholder="请输入密码" required />
      </div>

      <!-- 错误信息 -->
      <div v-if="errorMessage" class="error-message">
        <span>{{ errorMessage }}</span>
      </div>

      <!-- 登录按钮 -->
      <button type="submit" class="login-button" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>

      <!-- 注册链接 -->
      <div class="redirect-register">
        <p><a @click.prevent="redirectToRegister">注册</a>账号</p>
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

/* 登录标题 */
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

/* 登录按钮 */
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

/* 登录按钮悬停态 */
.login-button:hover {
  box-shadow: inset 400px 0 0 0 #2775b5;
}

/* 登录按钮激活态 */
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

/* 注册跳转提示 */
.redirect-register {
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.redirect-register a {
  color: #2980b9;
  cursor: pointer;
}
</style>
