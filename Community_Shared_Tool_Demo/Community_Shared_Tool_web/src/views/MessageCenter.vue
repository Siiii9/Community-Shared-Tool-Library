<!-- src/views/MessageCenter.vue -->
<template>
  <div class="message-center">
    <div class="operation-buttons">
      <button @click="refreshMessages">刷新</button>
      <button @click="markAllRead">全部标记为已读</button>
      <!-- 🔘 新增按钮 -->
      <button @click="simulateOverdueSoon" style="background-color: #ffedd5; color: #c2410c;">
        模拟至逾期前一小时
      </button>
    </div>
    <div class="message-list">
      <div v-for="msg in paginatedMessages" :key="msg.id" class="message-item" :class="{ 'unread': !msg.read }">
        <div class="message-header">
          <strong>{{ msg.title }}</strong>
          <!-- 🔴 提醒文字 -->
          <span v-if="msg.showReminder" style="color: #e63946; font-weight: bold; margin-left: 8px;">
            提醒：仅剩一小时
          </span>
          <span class="message-time">{{ formatDate(msg.time) }}</span>
        </div>
        <div class="message-content">{{ msg.content }}</div>
        <div class="message-actions">
          <button @click="markRead(msg)" v-if="!msg.read">标记已读</button>
        </div>
      </div>
      <div v-if="messages.length === 0" class="empty-message">
        暂无消息
      </div>
    </div>
    <div class="pagination">
      <button @click="changePage(1)" :disabled="currentPage === 1">首页</button>
      <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1">上一页</button>
      <span>第 {{ currentPage }} 页</span>
      <button @click="changePage(currentPage + 1)" :disabled="currentPage >= maxPage">下一页</button>
      <button @click="changePage(maxPage)" :disabled="currentPage >= maxPage">尾页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject } from 'vue'

// 👇 注入父组件提供的函数
const setOverdueReminder = inject('setOverdueReminder')

const messages = ref([])
const currentPage = ref(1)
const pageSize = 5

const formatDate = (isoStr) => {
  return new Date(isoStr).toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const refreshMessages = () => {
  messages.value = []
  currentPage.value = 1
  setOverdueReminder(false)
}

const markRead = (msg) => {
  msg.read = true
  // 如果所有消息都已读，清除红点
  if (!messages.value.some(m => !m.read)) {
    setOverdueReminder(false)
  }
}

const markAllRead = () => {
  messages.value.forEach(msg => msg.read = true)
  setOverdueReminder(false)
}

const simulateOverdueSoon = () => {
  messages.value = []
  currentPage.value = 1
  messages.value.push({
    id: 1,
    title: '借用1',
    content: '用户【test0】申请借用您的工具【电钻】，请尽快处理！',
    time: new Date().toISOString(),
    read: false,
    showReminder: true // 用于显示页面内的红色提醒
  })
  setOverdueReminder(true) // 👈 触发顶部“消息沟通”红点
}

const paginatedMessages = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return messages.value.slice(start, start + pageSize)
})

const maxPage = computed(() => Math.ceil(messages.value.length / pageSize))

const changePage = (page) => {
  if (page >= 1 && page <= maxPage.value) {
    currentPage.value = page
  }
}
</script>

<style scoped>
.message-center {
  padding: 20px;
}
.operation-buttons button {
  margin-right: 10px;
  padding: 6px 12px;
  background: #f8f9fa;
  border: 1px solid #ccc;
  border-radius: 4px;
  cursor: pointer;
}
.operation-buttons button:last-child {
  background-color: #ffedd5;
  color: #c2410c;
  border: 1px solid #fdba74;
}
.message-list {
  margin-top: 20px;
}
.message-item {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.message-item.unread {
  border-left: 4px solid #007bff;
  background: #f8f9ff;
}
.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 1.1em;
}
.message-time {
  color: #6c757d;
  font-size: 0.9em;
}
.message-content {
  color: #495057;
  margin: 8px 0;
}
.message-actions button {
  background: #007bff;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.9em;
}
.empty-message {
  text-align: center;
  color: #6c757d;
  padding: 40px 0;
}
.pagination {
  margin-top: 20px;
  text-align: center;
}
.pagination button {
  margin: 0 5px;
  padding: 6px 12px;
}
</style>