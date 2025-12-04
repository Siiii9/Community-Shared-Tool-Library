<!-- views/CommunityChat.vue -->
<template>
  <div class="community-chat">
    <div class="chat-header">
      <h2>社区聊天室</h2>
      <p>与邻居交流工具使用心得、发起互助请求</p>
    </div>

    <div class="chat-container">
      <!-- 聊天列表 -->
      <div class="chat-list">
        <div class="list-header">
          <h3>聊天列表</h3>
          <button class="new-chat-btn">
            <span class="material-icons">add</span>
            新建聊天
          </button>
        </div>
        
        <div class="chat-items">
          <div 
            v-for="chat in chatList" 
            :key="chat.id"
            :class="['chat-item', { active: activeChatId === chat.id }]"
            @click="selectChat(chat.id)"
          >
            <div class="chat-avatar">
              <span class="material-icons">person</span>
            </div>
            <div class="chat-info">
              <div class="chat-name">{{ chat.name }}</div>
              <div class="last-message">{{ chat.lastMessage }}</div>
              <div class="chat-time">{{ chat.time }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天窗口 -->
      <div class="chat-window">
        <div class="window-header">
          <div class="current-chat-info">
            <div class="chat-avatar">
              <span class="material-icons">group</span>
            </div>
            <div>
              <h3>{{ currentChat?.name || '选择聊天' }}</h3>
              <span class="online-status">{{ currentChat?.online || '离线' }}</span>
            </div>
          </div>
          <div class="window-actions">
            <button class="action-btn">
              <span class="material-icons">search</span>
            </button>
            <button class="action-btn">
              <span class="material-icons">more_vert</span>
            </button>
          </div>
        </div>

        <!-- 消息区域 -->
        <div class="messages-container">
          <div v-if="!currentChat" class="no-chat-selected">
            <span class="material-icons">forum</span>
            <p>选择一个聊天开始对话</p>
          </div>
          
          <div v-else class="messages">
            <div 
              v-for="message in currentChat.messages" 
              :key="message.id"
              :class="['message', { 'own-message': message.isOwn }]"
            >
              <div class="message-content">
                <div class="message-text">{{ message.text }}</div>
                <div class="message-time">{{ message.time }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div v-if="currentChat" class="input-area">
          <div class="input-container">
            <button class="attach-btn">
              <span class="material-icons">attach_file</span>
            </button>
            <input 
              type="text" 
              v-model="newMessage" 
              placeholder="输入消息..."
              class="message-input"
              @keypress="onKeyPress"
            >
            <button @click="sendMessage" class="send-btn">
              <span class="material-icons">send</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

// 模拟聊天数据
const chatList = ref([
  {
    id: 1,
    name: '社区工具共享群',
    lastMessage: '张三：谁有电钻可以借用？',
    time: '10:30',
    online: '在线 15人',
    messages: [
      { id: 1, text: '大家好，我是新来的邻居', time: '09:15', isOwn: false },
      { id: 2, text: '欢迎！有什么需要帮助的吗？', time: '09:16', isOwn: true },
      { id: 3, text: '谁有电钻可以借用？周末想装个书架', time: '10:30', isOwn: false }
    ]
  },
  {
    id: 2,
    name: '李四',
    lastMessage: '我明天可以还你梯子',
    time: '昨天',
    online: '在线',
    messages: [
      { id: 1, text: '梯子用完了吗？', time: '昨天 15:20', isOwn: true },
      { id: 2, text: '用完了，我明天可以还你', time: '昨天 15:22', isOwn: false },
      { id: 3, text: '好的，不急', time: '昨天 15:25', isOwn: true }
    ]
  },
  {
    id: 3,
    name: '王五',
    lastMessage: '那个割草机很好用',
    time: '周三',
    online: '2小时前在线',
    messages: [
      { id: 1, text: '割草机用着怎么样？', time: '周三 14:30', isOwn: true },
      { id: 2, text: '很好用，草坪修剪得很整齐', time: '周三 14:35', isOwn: false },
      { id: 3, text: '那就好，有需要随时说', time: '周三 14:40', isOwn: true }
    ]
  }
])

const activeChatId = ref(null)
const newMessage = ref('')

const currentChat = computed(() => {
  return chatList.value.find(chat => chat.id === activeChatId.value)
})

const selectChat = (chatId) => {
  activeChatId.value = chatId
}

// 发送消息功能
const sendMessage = () => {
  if (!newMessage.value.trim() || !currentChat.value) return
  
  const currentTime = new Date()
  const hours = currentTime.getHours().toString().padStart(2, '0')
  const minutes = currentTime.getMinutes().toString().padStart(2, '0')
  const timeString = `${hours}:${minutes}`
  
  // 创建新消息
  const newMessageObj = {
    id: Date.now(),
    text: newMessage.value.trim(),
    time: timeString,
    isOwn: true
  }
  
  // 添加到当前聊天
  currentChat.value.messages.push(newMessageObj)
  
  // 更新聊天列表的最后一条消息
  const chatIndex = chatList.value.findIndex(chat => chat.id === activeChatId.value)
  if (chatIndex !== -1) {
    chatList.value[chatIndex].lastMessage = `我：${newMessage.value.trim()}`
    chatList.value[chatIndex].time = timeString
  }
  
  // 清空输入框
  newMessage.value = ''
  
  // 模拟对方回复（可选）
  setTimeout(() => {
    if (currentChat.value) {
      const replyTime = new Date()
      const replyHours = replyTime.getHours().toString().padStart(2, '0')
      const replyMinutes = (replyTime.getMinutes() + 1).toString().padStart(2, '0')
      const replyTimeString = `${replyHours}:${replyMinutes}`
      
      const replyMessage = {
        id: Date.now() + 1,
        text: getRandomReply(),
        time: replyTimeString,
        isOwn: false
      }
      
      currentChat.value.messages.push(replyMessage)
      
      // 更新聊天列表的最后一条消息
      if (chatIndex !== -1) {
        chatList.value[chatIndex].lastMessage = `${currentChat.value.name.split(' ')[0]}：${replyMessage.text}`
        chatList.value[chatIndex].time = replyTimeString
      }
    }
  }, 1000 + Math.random() * 2000) // 1-3秒后回复
}

// 获取随机回复
const getRandomReply = () => {
  const replies = [
    '收到！',
    '好的，没问题',
    '这个建议不错',
    '我考虑一下',
    '谢谢分享',
    '明白了',
    '稍等我看一下',
    '这个工具我也有',
    '可以借用',
    '什么时候方便？'
  ]
  return replies[Math.floor(Math.random() * replies.length)]
}

// 支持回车发送
const onKeyPress = (event) => {
  if (event.key === 'Enter') {
    sendMessage()
  }
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

.chat-container {
  display: flex;
  height: calc(100vh - 180px);
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 聊天列表样式 */
.chat-list {
  width: 300px;
  border-right: 1px solid #e0e0e0;
  background: #f8f9fa;
}

.list-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-header h3 {
  margin: 0;
  color: #2c3e50;
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 12px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.new-chat-btn:hover {
  background: #2980b9;
}

.chat-items {
  overflow-y: auto;
  max-height: calc(100% - 80px);
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  cursor: pointer;
  border-bottom: 1px solid #e0e0e0;
  transition: background 0.2s;
}

.chat-item:hover {
  background: #e9ecef;
}

.chat-item.active {
  background: #3498db;
  color: white;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #3498db;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-name {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 0.95rem;
}

.last-message {
  color: #6c757d;
  font-size: 0.85rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.chat-time {
  color: #adb5bd;
  font-size: 0.75rem;
}

.chat-item.active .last-message,
.chat-item.active .chat-time {
  color: rgba(255, 255, 255, 0.8);
}

/* 聊天窗口样式 */
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.window-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
}

.current-chat-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.current-chat-info h3 {
  margin: 0;
  color: #2c3e50;
}

.online-status {
  color: #28a745;
  font-size: 0.85rem;
}

.window-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  background: none;
  border: none;
  color: #6c757d;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background 0.2s;
}

.action-btn:hover {
  background: #f8f9fa;
}

/* 消息区域 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
}

.no-chat-selected {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6c757d;
}

.no-chat-selected .material-icons {
  font-size: 4rem;
  margin-bottom: 20px;
  color: #adb5bd;
}

.messages {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message {
  display: flex;
}

.message.own-message {
  justify-content: flex-end;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
}

.message:not(.own-message) .message-content {
  background: white;
  border-bottom-left-radius: 4px;
}

.message.own-message .message-content {
  background: #3498db;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-text {
  margin-bottom: 4px;
  line-height: 1.4;
}

.message-time {
  font-size: 0.75rem;
  opacity: 0.7;
  text-align: right;
}

/* 输入区域 */
.input-area {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
  background: white;
}

.input-container {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f8f9fa;
  border-radius: 25px;
  padding: 8px 16px;
  transition: box-shadow 0.2s;
}

.input-container:focus-within {
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.attach-btn {
  background: none;
  border: none;
  color: #6c757d;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background 0.2s;
}

.attach-btn:hover {
  background: #e9ecef;
}

.message-input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  padding: 8px 0;
  font-size: 0.95rem;
}

.message-input::placeholder {
  color: #adb5bd;
}

.send-btn {
  background: #3498db;
  border: none;
  color: white;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn:hover {
  background: #2980b9;
  transform: scale(1.05);
}

.send-btn:active {
  transform: scale(0.95);
}

/* 滚动条样式 */
.chat-items::-webkit-scrollbar,
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.chat-items::-webkit-scrollbar-track,
.messages-container::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-items::-webkit-scrollbar-thumb,
.messages-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-items::-webkit-scrollbar-thumb:hover,
.messages-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
    height: calc(100vh - 140px);
  }
  
  .chat-list {
    width: 100%;
    height: 200px;
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .chat-items {
    max-height: 120px;
  }
  
  .chat-window {
    height: calc(100% - 200px);
  }
}

/* 动画效果 */
.chat-item {
  animation: fadeIn 0.3s ease-in-out;
}

.message {
  animation: slideIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

.message.own-message {
  animation: slideInRight 0.3s ease-out;
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}
</style>