<!-- views/CommunityChat.vue -->
<template>
  <div class="community-chat">
    <div class="chat-header">
      <h2>社区聊天室</h2>
      <p>与邻居交流工具使用心得、发起互助请求</p>
    </div>

    <div class="chat-placeholder">
      <div class="placeholder-content">
        <span class="material-icons">forum</span>
        <h3>即时聊天功能开发中</h3>
        <p>即将支持：群聊、@提醒、图片发送、消息历史</p>
        <button @click="goToMessageCenter" class="btn-primary">先去消息中心看看？</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToMessageCenter = () => {
  router.push('/message-center')
}
</script>

<style scoped>
.community-chat {
  padding: 20px;
  height: 100%;
}

.chat-header h2 {
  color: #2c3e50;
  margin-bottom: 8px;
}

.chat-header p {
  color: #7f8c8d;
  margin-bottom: 30px;
}

.chat-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: calc(100% - 100px);
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px dashed #dee2e6;
}

.placeholder-content {
  text-align: center;
  color: #6c757d;
}

.placeholder-content .material-icons {
  font-size: 4rem;
  color: #3498db;
  margin-bottom: 20px;
}

.placeholder-content h3 {
  margin-bottom: 10px;
  color: #2c3e50;
}

.btn-primary {
  margin-top: 20px;
  padding: 10px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #2980b9;
}
</style>