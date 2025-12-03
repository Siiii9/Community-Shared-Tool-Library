<!-- views/MessageCenter.vue -->
<template>
  <div class="message-center">
    <div class="operation-buttons">
      <button @click="refreshMessages">刷新</button>
      <button @click="markAllRead">全部标记为已读</button>
      <!-- 新增：模拟至逾期前一小时按钮 -->
      <button @click="simulateOverdueSoon" style="background-color: #ffedd5; color: #c2410c;">
        模拟至逾期前一小时
      </button>
    </div>
    <div class="message-list">
      <div v-for="msg in paginatedMessages" :key="msg.id" class="message-item" :class="{ 'unread': !msg.read }">
        <div class="message-header">
          <strong>{{ msg.title }}</strong>
          <!-- 新增：条件渲染提醒文本 -->
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
import { ref, computed, onMounted } from 'vue'

const messages = ref([])
const currentPage = ref(1)
const pageSize = 5

// 修改：在生成模拟数据时，为每条消息添加 showReminder 字段
const generateMockMessages = () => {
  return Array.from({ length: 12 }, (_, i) => ({
    id: i + 1,
    title: `借用申请 ${i + 1}`,
    content: `用户【test${i}】申请借用您的工具【电钻】，请及时处理。`,
    time: new Date(Date.now() - i * 3600000).toISOString(),
    read: i % 3 !== 0, // 部分未读
    showReminder: false // 👈 新增字段，默认不显示提醒
  }))
}

const paginatedMessages = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return messages.value.slice(start, start + pageSize)
})

const maxPage = computed(() => Math.ceil(messages.value.length / pageSize))

const formatDate = (isoStr) => {
  return new Date(isoStr).toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const refreshMessages = () => {
  messages.value = generateMockMessages()
}

const markRead = (msg) => {
  msg.read = true
}

const markAllRead = () => {
  messages.value.forEach(msg => msg.read = true)
}

// 新增：模拟至逾期前一小时的方法
const simulateOverdueSoon = () => {
  if (messages.value.length > 0) {
    // 精准定位 id 为 1 的第一条消息（即“借用申请 1”）
    const firstMsg = messages.value.find(msg => msg.id === 1)
    if (firstMsg) {
      firstMsg.showReminder = true
    }
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= maxPage.value) {
    currentPage.value = page
  }
}

onMounted(() => {
  refreshMessages()
})
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
/* 为新按钮添加样式 */
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