<!-- src/views/ToolDetail.vue -->
<template>
  <div class="tool-detail">
    <div class="detail-header">
      <h1>{{ tool?.name }}</h1>
      <div class="status-badge" :class="tool?.status">
        {{ tool?.status === 'available' ? '可用' : '已借出' }}
      </div>
    </div>

    <div class="detail-content">
      <!-- 图片展示（模拟） -->
      <div class="image-section">
        <img :src="toolImage" alt="工具图片" class="main-image" />
      </div>

      <!-- 工具介绍 -->
      <div class="description">
        <h3>工具介绍</h3>
        <p>{{ tool?.description || '暂无介绍' }}</p>
      </div>

      <!-- 操作按钮 -->
    <div class="action-buttons">
      <button class="chat-btn" @click="goToChat">
        <span class="material-icons">chat</span>
        聊一聊
      </button>
      <button 
        class="borrow-btn" 
        @click="showApplyDialog" 
        :disabled="tool?.status !== 'available'"
      >
        <span class="material-icons">shopping_cart</span>
        申请借用
      </button>
    </div>

    <!-- 借用申请对话框 -->
    <div v-if="showApplyForm" class="apply-dialog-overlay">
      <div class="apply-dialog">
        <h3>申请借用 {{ tool?.name }}</h3>
        <form @submit.prevent="submitApply">
          <div class="form-group">
            <label for="borrowDays">借用天数：</label>
            <input 
              type="number" 
              id="borrowDays" 
              v-model="applyForm.borrowDays" 
              min="1" 
              :max="tool?.borrowDaysLimit || 30" 
              required
            >
            <span class="limit-info">（最多可借用 {{ tool?.borrowDaysLimit || 30 }} 天）</span>
          </div>
          <div class="form-group">
            <label for="applyReason">借用原因：</label>
            <textarea 
              id="applyReason" 
              v-model="applyForm.applyReason" 
              rows="4" 
              placeholder="请简要说明您的借用原因"
              required
            ></textarea>
          </div>
          <div class="dialog-buttons">
            <button type="button" @click="cancelApply" class="cancel-btn">取消</button>
            <button type="submit" class="submit-btn">提交申请</button>
          </div>
        </form>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const tool = ref<any>(null)

// 从API加载工具详情
const loadToolById = async (id: string) => {
  try {
    const response = await axios.get(`/api/published-tools/${id}`)
    if (response.data) {
      tool.value = {
        id: response.data.id,
        name: response.data.toolName,
        status: response.data.status,
        location: response.data.location,
        description: response.data.description,
        borrowDaysLimit: response.data.borrowDaysLimit
      }
    }
  } catch (error) {
    console.error('获取工具详情失败:', error)
  }
}

// 模拟图片（实际项目中从后端获取）
const toolImage = ref('/images/tool-placeholder.jpg') // 你可以替换为真实图片路径

onMounted(async () => {
  await loadToolById(route.params.id as string)
})

// 跳转到社区聊天页
const goToChat = () => {
  router.push('/community-chat')
}

// 借用申请相关状态
const showApplyForm = ref(false)
const applyForm = ref({
  borrowDays: 3,
  applyReason: ''
})

// 显示申请对话框
const showApplyDialog = () => {
  if (tool.value?.status !== 'available') return
  showApplyForm.value = true
}

// 取消申请
const cancelApply = () => {
  showApplyForm.value = false
  applyForm.value = {
    borrowDays: 3,
    applyReason: ''
  }
}

// 提交借用申请
const submitApply = async () => {
  if (!applyForm.value.borrowDays || !applyForm.value.applyReason.trim()) {
    alert('请填写完整的借用信息')
    return
  }

  // 检查借用天数是否超过限制
  if (tool.value.borrowDaysLimit && applyForm.value.borrowDays > tool.value.borrowDaysLimit) {
    alert(`借用天数不能超过最大限制${tool.value.borrowDaysLimit}天`)
    return
  }

  try {
    // 动态获取用户ID和工具所有者ID
    const currentUserId = parseInt(localStorage.getItem('userId')) || 1 // 当前用户ID
    const toolOwnerId = 2 // 工具所有者ID

    const response = await axios.post('/api/borrow/apply', {
      toolId: tool.value.id,
      borrowerId: currentUserId,
      ownerId: toolOwnerId,
      borrowDays: applyForm.value.borrowDays,
      applyReason: applyForm.value.applyReason
    })

    if (response.data) {
      alert('✅ 借用申请提交成功！等待物品所有者确认。')
      tool.value.status = 'pending'
      showApplyForm.value = false
      applyForm.value = {
        borrowDays: 3,
        applyReason: ''
      }
    } else {
      alert('申请失败，请稍后重试')
    }
  } catch (error) {
    console.error('申请借用失败：', error)
    alert('申请失败，请稍后重试')
  }
}
</script>

<style scoped>
.tool-detail {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.detail-header h1 {
  color: #2c3e50;
  margin: 0;
  font-size: 1.8rem;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  color: white;
}

.status-badge.available {
  background: #52c41a;
}

.status-badge.borrowed {
  background: #faad14;
}

.image-section {
  text-align: center;
  margin-bottom: 20px;
}

.main-image {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.description {
  margin-bottom: 25px;
}

.description h3 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.description p {
  line-height: 1.7;
  color: #333;
  font-size: 1.05rem;
}

.action-buttons {
  display: flex;
  gap: 20px;
  justify-content: space-between;
}

.chat-btn, .borrow-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
}

/* 借用申请对话框样式 */
.apply-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.apply-dialog {
  background: white;
  padding: 30px;
  border-radius: 10px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.apply-dialog h3 {
  margin-bottom: 20px;
  color: #2c3e50;
  text-align: center;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group textarea {
  resize: vertical;
}

.dialog-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.cancel-btn {
  padding: 10px 20px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 5px;
  cursor: pointer;
}

.submit-btn {
  padding: 10px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.submit-btn:hover {
  background: #40a9ff;
  cursor: pointer;
  transition: all 0.3s;
}

.chat-btn {
  background: #607D8B;
  color: white;
  flex: 1;
}

.chat-btn:hover {
  background: #455A64;
}

.borrow-btn {
  background: #4CAF50;
  color: white;
  flex: 1;
}

.borrow-btn:hover:not(:disabled) {
  background: #388E3C;
}

.borrow-btn:disabled {
  background: #bdbdbd;
  cursor: not-allowed;
}

.material-icons {
  font-size: 18px;
}
</style>