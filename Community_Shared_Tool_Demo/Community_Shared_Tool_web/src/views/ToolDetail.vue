<!-- src/views/ToolDetail.vue -->
<template>
  <div class="tool-detail">
    <div class="detail-header">
      <button class="back-btn" @click="goBack">
        <span class="material-icons">arrow_back</span>
        返回
      </button>
      <h1>{{ tool?.name }}</h1>
      <div class="status-badge" :class="tool?.status">
        {{ tool?.status === 'available' ? '可用' : '已借出' }}
      </div>
    </div>
    <div class="detail-content">
      <!-- 图片展示 -->
      <div class="image-section">
        <img v-if="tool?.imageUrl" :src="getFullImageUrl(tool.imageUrl)" alt="工具图片" class="main-image" />
        <div v-else class="no-image-placeholder">
          <span class="material-icons">photo_camera</span>
          <p>暂无图片</p>
        </div>
      </div>
      <!-- 工具介绍 -->
      <div class="description">
        <h3>工具介绍</h3>
        <p>{{ tool?.description || '暂无介绍' }}</p>
        <p><strong>位置：</strong>{{ tool?.location }}</p>
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
    
    <!-- 支付押金对话框 -->
    <div v-if="showPaymentDialog" class="apply-dialog-overlay">
      <div class="apply-dialog">
        <h3>支付押金</h3>
        <div class="deposit-info">
          <p>工具名称：{{ tool?.name }}</p>
          <p>借用天数：{{ applyForm.borrowDays }} 天</p>
          <p class="deposit-amount">押金金额：<span class="amount">¥{{ depositAmount }}</span></p>
        </div>
        <div class="dialog-buttons">
          <button type="button" @click="cancelPayment" class="cancel-btn">取消</button>
          <button type="button" @click="processPayment" class="submit-btn">支付</button>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

// 使用与ToolMap.vue保持一致的测试数据，确保地图和详情页数据统一
const MOCK_TOOLS = [ 
  { id: 1, name: '冲击钻', lng: 116.235718, lat: 40.141605, location: '工学A座-105工具间', status: 'available', description: '用于在混凝土、金属和木材等材料上钻孔的电动工具', imageUrl: '/uploads/images/chongjizuan.png', borrowDaysLimit: 7 }, 
  { id: 2, name: '万用表', lng: 116.238418, lat: 40.142330, location: '信息C座-301电子室', status: 'available', description: '用于测量电压、电流和电阻的多功能电子测试工具', imageUrl: '/uploads/images/wanyongbiao.png', borrowDaysLimit: 14 }, 
  { id: 3, name: '电焊机', lng: 116.237475, lat: 40.141751, location: '信息A座-202车间', status: 'available', description: '通过电流加热金属部件实现焊接的设备', imageUrl: '/uploads/images/dianhan.png', borrowDaysLimit: 3 }, 
  { id: 4, name: '手电钻', lng: 116.236858, lat: 40.141954, location: '工学B座-101实验室', status: 'available', description: '便携式电动钻孔工具，适用于各种钻孔作业', imageUrl: '/uploads/images/shoudianzuan.png', borrowDaysLimit: 10 }, 
  { id: 5, name: '水平仪', lng: 116.238675, lat: 40.140950, location: '图书馆-工具角', status: 'borrowed', description: '用于测量水平和垂直角度的精密仪器', imageUrl: '/uploads/images/shuiping.png', borrowDaysLimit: 5 }, 
  { id: 6, name: '冲击钻', lng: 116.235878, lat: 40.141123, location: '文理楼B', status: 'available', description: '高性能冲击钻，适用于各种建筑和维修作业', imageUrl: '/uploads/images/chongjizuan.png', borrowDaysLimit: 7 }, 
  { id: 7, name: '万用表', lng: 116.235368, lat: 40.140513, location: '文理楼C', status: 'borrowed', description: '数字式万用表，具有高精度测量功能', imageUrl: '/uploads/images/wanyongbiao.png', borrowDaysLimit: 14 }, 
  { id: 8, name: '水平仪', lng: 116.236813, lat: 40.140505, location: '文理楼A', status: 'available', description: '激光水平仪，提供直观的水平和垂直线指示', imageUrl: '/uploads/images/shuiping.png', borrowDaysLimit: 5 }, 
  { id: 9, name: '万用表', lng: 116.240475, lat: 40.140618, location: '学生发展中心', status: 'available', description: '多功能万用表，适合电子爱好者和专业维修人员', imageUrl: '/uploads/images/wanyongbiao.png', borrowDaysLimit: 14 }, 
  { id: 10, name: '万用表', lng: 116.239474, lat: 40.142992, location: '瑞幸咖啡店', status: 'available', description: '便携式万用表，方便现场测试使用', imageUrl: '/uploads/images/wanyongbiao.png', borrowDaysLimit: 14 }, 
]
const route = useRoute()
const router = useRouter()
const tool = ref<any>(null)
// 加载工具详情 - 优先从后端API获取真实数据
const loadToolById = async (id: string) => {
  try {
    const numericId = parseInt(id)
    // 先从后端API获取真实数据
    const response = await axios.get(`/api/published-tools/${numericId}`)
    const data = response.data
    
    if (data.success && data.data) {
      // 处理后端返回的数据，映射到前端需要的字段
      const backendTool = data.data
      tool.value = {
        id: backendTool.id,
        name: backendTool.toolName || backendTool.name,
        status: backendTool.status,
        location: backendTool.location,
        description: backendTool.description,
        imageUrl: backendTool.imageUrl,
        borrowDaysLimit: backendTool.borrowDaysLimit,
        ownerId: backendTool.ownerId,
        // 添加经纬度信息
        lng: backendTool.longitude,
        lat: backendTool.latitude
      }
      return
    }
    
    // 如果后端数据不完整，尝试使用模拟数据作为备选
    console.warn('后端数据不完整，尝试使用模拟数据')
    const mockTool = MOCK_TOOLS.find(t => t.id === numericId)
    if (mockTool) {
      tool.value = {
        ...mockTool,
        ownerId: 1 // 添加默认所有者ID
      }
    } else {
      throw new Error('工具ID不存在')
    }
  } catch (error) {
    console.error('加载工具详情失败:', error)
    // 显示一个友好的错误提示，而不是alert
    const numericId = parseInt(id)
    tool.value = {
      id: numericId,
      name: '未知工具',
      status: 'available',
      location: '未知位置',
      description: '未找到该工具的详细信息',
      imageUrl: '',
      borrowDaysLimit: 30,
      ownerId: 1
    }
  }
}
// 获取完整图片URL
const getFullImageUrl = (imageUrl: string) => {
  if (!imageUrl) return ''
  // 如果已经是完整URL或DataURL，直接返回
  if (imageUrl.startsWith('http') || imageUrl.startsWith('data:')) {
    return imageUrl
  }
  // 如果是相对路径，添加后端服务器地址
  const baseUrl = 'http://localhost:8084'
  return baseUrl + imageUrl
}
onMounted(async () => {
  const id = route.params.id as string
  if (id) {
    await loadToolById(id)
  } else {
    console.error('无效的工具ID')
  }
})
// 返回地图页面
const goBack = () => {
  router.push('/tool-map')
}

// 跳转到社区聊天页
const goToChat = () => {
  router.push('/community-chat')
}
// 借用申请相关状态
const showApplyForm = ref(false)
const showPaymentDialog = ref(false)
const depositAmount = ref(200) // 默认押金金额，实际应根据工具类型和借用天数计算
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
// 取消支付
const cancelPayment = () => {
  showPaymentDialog.value = false
}
// 处理支付逻辑
const processPayment = async () => {
  try {
    // 模拟支付过程
    // 在实际项目中，这里应该调用支付接口
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 支付成功后提交借用申请
    await submitBorrowApplication()
    
    // 隐藏支付对话框
    showPaymentDialog.value = false
  } catch (error: any) {
    console.error('支付失败：', error)
    alert(`支付失败：${error.message || '请稍后重试'}`)
  }
}
// 提交借用申请的核心逻辑
const submitBorrowApplication = async () => {
  try {
    // 获取当前用户ID
    const userIdStr = localStorage.getItem('userId')
    const borrowerId = userIdStr ? parseInt(userIdStr) : 1 // 默认用户ID为1
    
    // 确保所有参数都是正确的类型
    const params = {
      toolId: parseInt(tool.value.id), // 确保是整数
      borrowerId: borrowerId, // 已经是整数类型，无需再次转换
      ownerId: tool.value.ownerId, // 已经是整数类型，无需再次转换
      borrowDays: applyForm.value.borrowDays, // 已经是整数类型，无需再次转换
      applyReason: applyForm.value.applyReason.trim() // 确保字符串且去除首尾空格
    }
    
    console.log('提交借用申请参数：', params)
    
    // 调用后端API提交借用申请
    const response = await axios.post('/api/borrow/apply', params)
    
    if (response.data.success) {
      alert('✅ 借用申请提交成功！等待物品所有者确认。')
      // 注意：这里不应该直接修改tool的状态，而应该重新加载工具信息
      showApplyForm.value = false
      applyForm.value = {
        borrowDays: 3,
        applyReason: ''
      }
    } else {
      throw new Error(response.data.message || '申请失败')
    }
  } catch (error: any) {
    console.error('申请借用失败：', error)
    // 显示更详细的错误信息
    const errorMessage = error.response?.data?.message || error.message || '请稍后重试'
    alert(`申请失败：${errorMessage}`)
  }
}


// 提交借用申请 - 验证后显示支付对话框
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
  
  // 计算押金金额（示例：按每天10元计算，最低200元）
  depositAmount.value = Math.max(200, applyForm.value.borrowDays * 10)
  
  // 显示支付押金对话框
  showPaymentDialog.value = true
}
</script>
<style scoped>
.tool-detail {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  max-height: calc(100vh - 60px);
  overflow-y: auto;
}
.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  position: relative;
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
  border: 2px solid #ffd700; /* 添加黄色边框 */
  box-shadow: 0 0 0 1px rgba(255, 215, 0, 0.3); /* 添加黄色阴影效果 */
}
.image-section {
  text-align: center;
  margin-bottom: 20px;
}
.main-image {
  width: 100%;
  max-height: 500px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  background-color: #f5f5f5;
}
.no-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  background: #f5f5f5;
  border-radius: 8px;
  color: #999;
}
.no-image-placeholder .material-icons {
  font-size: 48px;
  margin-bottom: 10px;
}
.no-image-placeholder p {
  margin: 0;
  font-size: 16px;
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
.deposit-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 5px;
}
.deposit-info p {
  margin: 10px 0;
  font-size: 14px;
  color: #333;
}
.deposit-amount {
  font-size: 16px !important;
  font-weight: bold;
  margin-top: 15px !important;
  padding-top: 15px;
  border-top: 1px solid #eee;
}
.deposit-amount .amount {
  color: #ff4d4f;
  font-size: 18px;
  margin-left: 5px;
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
.back-btn {
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.back-btn:hover {
  background: #e0e0e0;
}
</style>