<!-- src/views/BorrowApplications.vue -->
<template>
  <div class="borrow-applications">
    <h2>借用申请管理</h2>
    
    <div class="filter-section">
      <button @click="refreshApplications" class="refresh-btn">刷新</button>
      <select v-model="filterStatus" @change="filterApplications">
        <option value="">全部状态</option>
        <option value="PENDING">待处理</option>
        <option value="APPROVED">已同意</option>
        <option value="REJECTED">已拒绝</option>
        <option value="TAKEN">已取用</option>
        <option value="RETURNED">已归还</option>
      </select>
    </div>

    <div class="applications-list">
      <div v-if="applications.length === 0" class="no-applications">
        <p>暂无借用申请</p>
      </div>
      
      <div v-else class="application-item" v-for="application in filteredApplications" :key="application.id">
        <div class="application-header">
          <h3>{{ getToolName(application.toolId) }}</h3>
          <span :class="['status-badge', application.status.toLowerCase()]">
            {{ getStatusText(application.status) }}
          </span>
        </div>
        
        <div class="application-details">
          <p><strong>申请人：</strong>用户 {{ application.borrowerId }}</p>
          <p><strong>申请时间：</strong>{{ formatDate(application.applyTime) }}</p>
          <p><strong>借用天数：</strong>{{ application.borrowDays }} 天</p>
          <p><strong>预计归还：</strong>{{ formatDate(application.expectedReturnTime) }}</p>
          <p><strong>借用原因：</strong>{{ application.applyReason }}</p>
          
          <div v-if="application.rejectReason" class="reject-reason">
            <strong>拒绝原因：</strong>{{ application.rejectReason }}
          </div>
          
          <div v-if="application.approveTime" class="approve-time">
            <strong>同意时间：</strong>{{ formatDate(application.approveTime) }}
          </div>
          
          <div v-if="application.takeTime" class="take-time">
            <strong>取用时间：</strong>{{ formatDate(application.takeTime) }}
          </div>
          
          <div v-if="application.returnTime" class="return-time">
            <strong>归还时间：</strong>{{ formatDate(application.returnTime) }}
          </div>
        </div>
        
        <div class="application-actions" v-if="application.status === 'PENDING'">
          <button @click="approveApplication(application.id)" class="approve-btn">同意</button>
          <button @click="showRejectDialog(application.id)" class="reject-btn">拒绝</button>
        </div>
        
        <div class="application-actions" v-if="application.status === 'APPROVED'">
          <button @click="confirmTake(application.id)" class="take-btn">确认取用</button>
        </div>
        
        <div class="application-actions" v-if="application.status === 'TAKEN'">
          <button @click="confirmReturn(application.id)" class="return-btn">确认归还</button>
        </div>
      </div>
    </div>

    <!-- 拒绝原因对话框 -->
    <div v-if="showRejectForm" class="reject-dialog-overlay">
      <div class="reject-dialog">
        <h3>拒绝借用申请</h3>
        <form @submit.prevent="submitReject">
          <div class="form-group">
            <label for="rejectReason">拒绝原因：</label>
            <textarea 
              id="rejectReason" 
              v-model="rejectForm.rejectReason" 
              rows="4" 
              placeholder="请说明拒绝的原因"
              required
            ></textarea>
          </div>
          <div class="dialog-buttons">
            <button type="button" @click="cancelReject" class="cancel-btn">取消</button>
            <button type="submit" class="submit-btn">确认拒绝</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

interface BorrowApplication {
  id: number
  toolId: number
  borrowerId: number
  ownerId: number
  status: string
  applyTime: string
  approveTime: string | null
  takeTime: string | null
  returnTime: string | null
  expectedReturnTime: string
  borrowDays: number
  applyReason: string
  rejectReason: string | null
}

const applications = ref<BorrowApplication[]>([])
const filterStatus = ref('')
const showRejectForm = ref(false)
const rejectForm = ref({
  applicationId: 0,
  rejectReason: ''
})

// 模拟工具数据
const tools = ref([
  { id: 1, name: '梯子' },
  { id: 2, name: '冲击钻' },
  { id: 3, name: '万用表' },
  { id: 4, name: '电焊机' },
  { id: 5, name: '手电钻' },
  { id: 6, name: '水平仪' }
])

// 获取工具名称
const getToolName = (toolId: number) => {
  const tool = tools.value.find(t => t.id === toolId)
  return tool ? tool.name : '未知工具'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'PENDING': '待处理',
    'APPROVED': '已同意',
    'REJECTED': '已拒绝',
    'TAKEN': '已取用',
    'RETURNED': '已归还'
  }
  return statusMap[status] || status
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '--'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 过滤申请
const filteredApplications = computed(() => {
  if (!filterStatus.value) return applications.value
  return applications.value.filter(app => app.status === filterStatus.value)
})

// 刷新申请列表
const refreshApplications = async () => {
  try {
    // 模拟当前用户ID（实际项目中应从登录状态获取）
    const currentUserId = parseInt(localStorage.getItem('userId')) || 2 // 工具所有者ID
    
    const response = await axios.get(`/api/borrow/applications/${currentUserId}`)
    if (response.data.success) {
      applications.value = response.data.data
    }
  } catch (error) {
    console.error('获取申请列表失败：', error)
    // 模拟数据
    applications.value = [
      {
        id: 1,
        toolId: 1,
        borrowerId: 1,
        ownerId: 2,
        status: 'PENDING',
        applyTime: new Date().toISOString(),
        approveTime: null,
        takeTime: null,
        returnTime: null,
        expectedReturnTime: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toISOString(),
        borrowDays: 3,
        applyReason: '需要维修实验室的设备',
        rejectReason: null
      }
    ]
  }
}

// 同意申请
const approveApplication = async (applicationId: number) => {
  if (!confirm('确定同意此借用申请？')) return
  
  try {
    const response = await axios.post(`/api/borrow/approve/${applicationId}`)
    if (response.data.success) {
      alert('已同意借用申请')
      await refreshApplications()
    }
  } catch (error) {
    console.error('同意申请失败：', error)
    alert('操作失败，请重试')
  }
}

// 显示拒绝对话框
const showRejectDialog = (applicationId: number) => {
  rejectForm.value.applicationId = applicationId
  rejectForm.value.rejectReason = ''
  showRejectForm.value = true
}

// 取消拒绝
const cancelReject = () => {
  showRejectForm.value = false
  rejectForm.value = {
    applicationId: 0,
    rejectReason: ''
  }
}

// 提交拒绝
const submitReject = async () => {
  if (!rejectForm.value.rejectReason.trim()) {
    alert('请填写拒绝原因')
    return
  }
  
  try {
    const response = await axios.post(`/api/borrow/reject/${rejectForm.value.applicationId}`, {
      rejectReason: rejectForm.value.rejectReason
    })
    
    if (response.data.success) {
      alert('已拒绝借用申请')
      showRejectForm.value = false
      await refreshApplications()
    }
  } catch (error) {
    console.error('拒绝申请失败：', error)
    alert('操作失败，请重试')
  }
}

// 确认取用
const confirmTake = async (applicationId: number) => {
  if (!confirm('确认用户已取用工具？')) return
  
  try {
    const response = await axios.post(`/api/borrow/take/${applicationId}`)
    if (response.data.success) {
      alert('已确认取用')
      await refreshApplications()
    }
  } catch (error) {
    console.error('确认取用失败：', error)
    alert('操作失败，请重试')
  }
}

// 确认归还
const confirmReturn = async (applicationId: number) => {
  if (!confirm('确认用户已归还工具？')) return
  
  try {
    const response = await axios.post(`/api/borrow/return/${applicationId}`)
    if (response.data.success) {
      alert('已确认归还')
      await refreshApplications()
    }
  } catch (error) {
    console.error('确认归还失败：', error)
    alert('操作失败，请重试')
  }
}

// 过滤申请
const filterApplications = () => {
  // 过滤逻辑在 computed 属性中处理
}

onMounted(() => {
  refreshApplications()
})
</script>

<style scoped>
.borrow-applications {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.filter-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}

.refresh-btn {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.filter-section select {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.applications-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.application-item {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  background: white;
}

.application-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.application-header h3 {
  margin: 0;
  color: #2c3e50;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.status-badge.pending {
  background: #faad14;
}

.status-badge.approved {
  background: #52c41a;
}

.status-badge.rejected {
  background: #ff4d4f;
}

.status-badge.taken {
  background: #1890ff;
}

.status-badge.returned {
  background: #722ed1;
}

.application-details p {
  margin: 5px 0;
  color: #666;
}

.reject-reason, .approve-time, .take-time, .return-time {
  margin-top: 10px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 5px;
}

.application-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

.approve-btn, .reject-btn, .take-btn, .return-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}

.approve-btn {
  background: #52c41a;
  color: white;
}

.reject-btn {
  background: #ff4d4f;
  color: white;
}

.take-btn {
  background: #1890ff;
  color: white;
}

.return-btn {
  background: #722ed1;
  color: white;
}

.no-applications {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* 拒绝对话框样式 */
.reject-dialog-overlay {
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

.reject-dialog {
  background: white;
  padding: 30px;
  border-radius: 10px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.reject-dialog h3 {
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

.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  resize: vertical;
  box-sizing: border-box;
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
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.submit-btn:hover {
  background: #ff7875;
}
</style>