<template>
  <div class="my-published">
    <div class="operation-buttons">
      <button @click="refreshData">刷新</button>
      <button @click="exportPublishedList">导出发布记录</button>
      <!-- 🔹 修改：按钮文字为黑色，无悬停变色 -->
      <button @click="openAddToolDialog" class="add-tool-btn">发布新工具</button>
    </div>

    <div class="filter-form">
      <label for="toolName">工具名称</label>
      <input v-model="filter.toolName" id="toolName" placeholder="输入工具名称" />

      <label for="toolStatus">工具状态</label>
      <select v-model="filter.status" id="toolStatus">
        <option value="">全部状态</option>
        <option value="available">可借用</option>
        <option value="borrowed">已借出</option>
        <option value="pending">申请中</option>
        <option value="maintenance">维护中</option>
      </select>

      <button @click="applyFilter">筛选</button>
      <button @click="resetFilter">重置</button>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th @click="sortData('publishTime')">发布时间</th>
          <th>工具名称</th>
          <th>位置</th>
          <th>状态</th>
          <th>借用提示</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(tool, index) in paginatedData" :key="tool.id">
          <td>{{ formatDate(tool.publishTime) }}</td>
          <td>{{ tool.toolName }}</td>
          <td>{{ tool.location }}</td>
          <td>
            <span
              :class="{
                'status-available': tool.status === 'available',
                'status-borrowed': tool.status === 'borrowed',
                'status-pending': tool.status === 'pending',
                'status-maintenance': tool.status === 'maintenance'
              }"
            >
              {{ statusText[tool.status] }}
            </span>
          </td>
          <td>
            <div v-if="pendingApplications[tool.id] && pendingApplications[tool.id].length > 0" class="pending-applications">
              <div v-for="application in pendingApplications[tool.id]" :key="application.id" class="application-item">
                <div class="application-info">
                  <span class="application-status">申请中</span>
                  <span class="borrower">用户 {{ application.borrowerId }}</span>
                </div>
                <div class="application-actions">
                  <button @click="approveApplication(application.id, tool.id)" class="btn-approve">同意</button>
                  <button @click="rejectApplication(application.id, tool.id)" class="btn-reject">拒绝</button>
                </div>
              </div>
            </div>
            <div v-else-if="waitingReturnConfirmations[tool.id] && waitingReturnConfirmations[tool.id].length > 0" class="return-confirmations">
              <div v-for="application in waitingReturnConfirmations[tool.id]" :key="application.id" class="application-item">
                <div class="application-info">
                  <span class="application-status">等待归还确认</span>
                  <span class="borrower">用户 {{ application.borrowerId }}</span>
                </div>
                <div class="application-actions">
                  <button @click="confirmReturn(application.id, tool.id)" class="btn-approve">确认归还</button>
                  <button @click="rejectReturn(application.id, tool.id)" class="btn-reject">拒绝归还</button>
                </div>
              </div>
            </div>
            <span v-else-if="tool.status === 'borrowed'" class="borrowing-status">
              借用中
            </span>
            <span v-else class="no-applications">
              -无-
            </span>
          </td>
          <td>
            <button @click="editTool(tool)" class="btn-edit">编辑</button>
            <button @click="deleteTool(tool.id)" class="btn-delete">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="pagination">
      <button @click="changePage(1)" :disabled="pagination.currentPage === 1">首页</button>
      <button @click="changePage(pagination.currentPage - 1)" :disabled="pagination.currentPage === 1">上一页</button>
      <span>第 {{ pagination.currentPage }} 页</span>
      <button @click="changePage(pagination.currentPage + 1)" :disabled="pagination.currentPage === maxPage">下一页</button>
      <button @click="changePage(maxPage)" :disabled="pagination.currentPage === maxPage">尾页</button>
    </div>

    <!-- 发布新工具对话框 -->
    <div v-if="showAddToolDialog" class="add-tool-dialog-overlay">
      <div class="add-tool-dialog">
        <h3>{{ newTool.id ? '编辑工具' : '发布新工具' }}</h3>
        <form @submit.prevent="saveTool">
          <div class="form-group">
            <label for="newToolName">工具名称：</label>
            <input 
              id="newToolName" 
              v-model="newTool.toolName" 
              required
            />
          </div>
          <div class="form-group">
            <label for="newDescription">描述：</label>
            <textarea 
              id="newDescription" 
              v-model="newTool.description" 
              rows="4"
            ></textarea>
          </div>
          <div class="form-group">
            <label for="newLocation">位置：</label>
            <input 
              id="newLocation" 
              v-model="newTool.location" 
              required
            />
          </div>
          <div class="form-group">
            <label for="newBorrowDaysLimit">最大借用天数：</label>
            <input 
              id="newBorrowDaysLimit" 
              v-model="newTool.borrowDaysLimit" 
              type="number" 
              min="1" 
              max="30" 
              required
            />
          </div>
          <!-- 🔹 恢复：添加照片功能 -->
          <div class="form-group">
            <label for="newImageUrl">工具图片：</label>
            <input 
              id="newImageUrl" 
              type="file" 
              accept="image/*"
              @change="handleImageUpload"
            />
            <div v-if="newTool.imageUrl" class="preview-image">
              <img :src="newTool.imageUrl" alt="预览" />
            </div>
          </div>
          <div class="dialog-buttons">
            <button type="button" @click="cancelAddTool" class="cancel-btn">取消</button>
            <button type="submit" class="submit-btn">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

// 状态映射
const statusText = {
  available: '可借用',
  borrowed: '已借出',
  maintenance: '维护中',
  pending: '申请中'
}

// 当前登录用户ID
const currentUserId = parseInt(localStorage.getItem('userId') || '1')

// 数据状态
const rawData = ref([])
const pendingApplications = ref({})
const waitingReturnConfirmations = ref({})
const filter = ref({ toolName: '', status: '' })
const sort = ref({ prop: null, order: null })
const pagination = ref({ currentPage: 1, pageSize: 5 })

// 变量：控制对话框显示
const showAddToolDialog = ref(false)
const newTool = ref({
  toolName: '',
  description: '',
  location: '',
  status: 'available',
  borrowDaysLimit: 7,
  imageUrl: '',
  id: null // 用于区分新增和编辑
})

const filteredData = computed(() => {
  return rawData.value.filter(item => {
    const nameMatch = item.toolName.includes(filter.value.toolName)
    const statusMatch = filter.value.status ? item.status === filter.value.status : true
    return nameMatch && statusMatch
  })
})

const sortedData = computed(() => {
  if (!sort.value.prop) return filteredData.value
  return [...filteredData.value].sort((a, b) => {
    const order = sort.value.order === 'ascending' ? 1 : -1
    return a[sort.value.prop] > b[sort.value.prop] ? order : -order
  })
})

const paginatedData = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize
  return sortedData.value.slice(start, start + pagination.value.pageSize)
})

const maxPage = computed(() => Math.ceil(filteredData.value.length / pagination.value.pageSize))

const formatDate = (isoStr) => {
  if (!isoStr) return '—'
  return new Date(isoStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取待处理的借用申请
const fetchPendingApplications = async () => {
  try {
    const response = await axios.get(`/api/borrow/my-applications/${currentUserId}`)
    // 处理后端返回的新响应格式
    const result = response.data
    const applications = result.success ? result.data : []
    
    // 筛选出PENDING状态的借用申请
    const pendingApps = applications.filter(app => app.status === 'PENDING')
    
    // 按工具ID分组
    const grouped = {}
    pendingApps.forEach(app => {
      if (!grouped[app.toolId]) {
        grouped[app.toolId] = []
      }
      grouped[app.toolId].push(app)
    })
    
    pendingApplications.value = grouped
  } catch (error) {
    console.error('获取借用申请出错:', error)
    console.log('使用模拟借用申请数据')
    // 使用模拟数据
    pendingApplications.value = {
      1: [
        {
          id: 1,
          toolId: 1,
          borrowerId: 2,
          status: 'PENDING',
          applyTime: new Date().toISOString(),
          borrowDays: 3
        }
      ]
    }
  }
}

// 获取等待归还确认的记录
const fetchWaitingReturnConfirmations = async () => {
  try {
    const response = await axios.get(`/api/borrow/my-applications/${currentUserId}`)
    const result = response.data
    
    // 处理后端返回的新响应格式
    const applications = result.success ? result.data : []
    
    // 筛选出等待归还确认的记录
    const waitingReturnApps = applications.filter(app => app.status === 'WAITING_RETURN_CONFIRM')
    
    // 按工具ID分组
    const grouped = {}
    waitingReturnApps.forEach(app => {
      if (!grouped[app.toolId]) {
        grouped[app.toolId] = []
      }
      grouped[app.toolId].push(app)
    })
    
    waitingReturnConfirmations.value = grouped
  } catch (error) {
    console.error('获取等待归还确认记录出错:', error)
    console.log('使用模拟等待归还确认数据')
    // 使用模拟数据
    waitingReturnConfirmations.value = {
      2: [
        {
          id: 2,
          toolId: 2,
          borrowerId: 3,
          status: 'WAITING_RETURN_CONFIRM',
          applyTime: new Date().toISOString(),
          borrowDays: 5
        }
      ]
    }
  }
}

// 同意借用申请
const approveApplication = async (applicationId, toolId) => {
  try {
    const response = await axios.post(`/api/borrow/approve/${applicationId}`)
    
    const result = response.data
    // 处理可能的不同响应格式
    if (result.success === false) {
      throw new Error(result.message || '同意借用申请失败')
    }
    
    // 更新工具状态
    const toolIndex = rawData.value.findIndex(t => t.id === toolId)
    if (toolIndex !== -1) {
      rawData.value[toolIndex].status = 'borrowed'
    }
    
    // 重新获取借用申请
    await fetchPendingApplications()
    
    alert('同意借用申请成功')
  } catch (error) {
    console.error('同意借用申请出错:', error)
    alert(error.message || '操作失败，请重试')
  }
}

// 拒绝借用申请
const rejectApplication = async (applicationId, toolId) => {
  // 使用alert替代prompt，避免浏览器兼容性问题
  // 后续可以考虑使用模态框组件
  const rejectReason = '不符合借用条件' // 默认拒绝原因
  
  try {
    const response = await axios.post(`/api/borrow/reject/${applicationId}`, { rejectReason })
    
    const result = response.data
    if (!result.success) {
      throw new Error(result.message || '拒绝借用申请失败')
    }
    
    // 更新工具状态为可借用
    const toolIndex = rawData.value.findIndex(t => t.id === toolId)
    if (toolIndex !== -1) {
      rawData.value[toolIndex].status = 'available'
    }
    
    // 重新获取借用申请
    await fetchPendingApplications()
    
    alert('拒绝借用申请成功')
  } catch (error) {
    console.error('拒绝借用申请出错:', error)
    alert('操作失败，请重试')
  }
}

// 确认归还
const confirmReturn = async (applicationId, toolId) => {
  if (!confirm('确认用户已归还工具？')) return
  
  try {
    const response = await axios.post(`/api/borrow/confirm-return/${applicationId}`)
    
    const result = response.data
    if (!result.success) {
      throw new Error(result.message || '确认归还失败')
    }
    
    // 更新工具状态为可借用
    const toolIndex = rawData.value.findIndex(t => t.id === toolId)
    if (toolIndex !== -1) {
      rawData.value[toolIndex].status = 'available'
    }
    
    // 重新获取等待归还确认记录
    await fetchWaitingReturnConfirmations()
    
    alert('确认归还成功')
  } catch (error) {
    console.error('确认归还出错:', error)
    alert(error.message || '操作失败，请重试')
  }
}

// 拒绝归还
const rejectReturn = async (applicationId, toolId) => {
  if (!confirm('确认拒绝用户的归还请求？')) return
  
  try {
    const response = await axios.post(`/api/borrow/reject-return/${applicationId}`)
    
    const result = response.data
    if (!result.success) {
      throw new Error(result.message || '拒绝归还失败')
    }
    
    // 重新获取等待归还确认记录
    await fetchWaitingReturnConfirmations()
    
    alert('拒绝归还成功')
  } catch (error) {
    console.error('拒绝归还出错:', error)
    alert(error.message || '操作失败，请重试')
  }
}

// 刷新数据
const refreshData = async () => {
  try {
    // 获取发布的工具
    const response = await axios.get(`/api/published-tools/owner/${currentUserId}`)
    rawData.value = response.data
    
    // 获取借用申请和等待归还确认记录
    await fetchPendingApplications()
    await fetchWaitingReturnConfirmations()
  } catch (error) {
    console.error('获取发布工具列表失败：', error)
    alert('获取发布工具列表失败，请重试')
  }
}

const applyFilter = () => {
  pagination.value.currentPage = 1
}

const resetFilter = () => {
  filter.value = { toolName: '', status: '' }
  pagination.value.currentPage = 1
}

const sortData = (prop) => {
  if (sort.value.prop === prop) {
    sort.value.order = sort.value.order === 'ascending' ? 'descending' : 'ascending'
  } else {
    sort.value.prop = prop
    sort.value.order = 'ascending'
  }
}

const exportPublishedList = () => {
  const csvContent = [
    '发布时间,工具名称,位置,状态',
    ...sortedData.value.map(item =>
      `"${formatDate(item.publishTime)}","${item.toolName}","${item.location}","${statusText[item.status]}"`
    )
  ].join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `my_published_tools_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
}

// 🔹 修复：函数名改为 openAddToolDialog
const openAddToolDialog = () => {
  newTool.value = {
    toolName: '',
    description: '',
    location: '',
    status: 'available',
    borrowDaysLimit: 7,
    imageUrl: '',
    id: null
  }
  showAddToolDialog.value = true
}

// 隐藏发布新工具对话框
const cancelAddTool = () => {
  showAddToolDialog.value = false
}

// 处理图片上传
const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      newTool.value.imageUrl = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

// 保存工具（新增或编辑）
const saveTool = async () => {
  try {
    newTool.value.ownerId = currentUserId
    let response
    
    // 🔹 修复：创建一个干净的工具对象，只包含后端需要的字段
    const toolData = {
      toolName: newTool.value.toolName,
      description: newTool.value.description,
      location: newTool.value.location,
      status: newTool.value.status,
      borrowDaysLimit: newTool.value.borrowDaysLimit,
      imageUrl: newTool.value.imageUrl,
      ownerId: currentUserId
    }
    
    if (newTool.value.id) {
      // 编辑
      toolData.id = newTool.value.id
      response = await axios.put(`/api/published-tools/${newTool.value.id}`, toolData)
      const index = rawData.value.findIndex(item => item.id === newTool.value.id)
      if (index !== -1) {
        rawData.value[index] = response.data
      }
      alert('✅ 工具编辑成功！')
    } else {
      // 新增
      response = await axios.post('/api/published-tools', toolData)
      rawData.value.push(response.data)
      alert('✅ 新工具发布成功！')
    }
    showAddToolDialog.value = false
  } catch (error) {
    console.error('保存工具失败：', error)
    if (error.response?.data?.message) {
      alert('保存失败：' + error.response.data.message)
    } else {
      alert('保存工具失败，请重试')
    }
  }
}

// 编辑工具
const editTool = (tool) => {
  // 🔹 修复：不包含 toolType 字段
  newTool.value = {
    id: tool.id,
    toolName: tool.toolName,
    description: tool.description,
    location: tool.location,
    status: tool.status,
    borrowDaysLimit: tool.borrowDaysLimit,
    imageUrl: tool.imageUrl
  }
  showAddToolDialog.value = true
}

// 删除工具
const deleteTool = async (id) => {
  if (confirm('确定删除该工具？')) {
    try {
      await axios.delete(`/api/published-tools/${id}`, {
        headers: {
          'X-User-Id': currentUserId
        }
      })
      rawData.value = rawData.value.filter(item => item.id !== id)
      alert('删除成功！')
    } catch (error) {
      console.error('删除工具失败：', error)
      alert('删除工具失败，请重试')
    }
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= maxPage.value) {
    pagination.value.currentPage = page
  }
}

onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.my-published {
  padding: 20px;
}

.operation-buttons button,
.filter-form button {
  margin-right: 10px;
  padding: 6px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background: #f8f9fa;
  cursor: pointer;
}

.add-tool-btn {
  /* 🔹 修复：按钮文字为黑色，无悬停变色 */
  color: black;
  background: white;
  border: 1px solid #ccc;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.add-tool-btn:hover {
  /* 🔹 修复：移除悬停变色 */
  background: white;
  color: black;
}

.filter-form {
  margin: 15px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-form input,
.filter-form select {
  padding: 6px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
}

.data-table th,
.data-table td {
  padding: 10px;
  text-align: left;
  border: 1px solid #eee;
}

.data-table th {
  background: #f5f7fa;
  cursor: pointer;
  user-select: none;
}

.status-available {
  background: #52c41a;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-borrowed {
  background: #faad14;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-maintenance {
  background: #722ed1;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-pending {
  background: #1890ff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.btn-edit {
  padding: 4px 8px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-edit:hover {
  background: #40a9ff;
}

.btn-delete {
  padding: 4px 8px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-delete:hover {
  background: #dc3545;
}

.add-tool-dialog-overlay {
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

.add-tool-dialog {
  background: white;
  padding: 30px;
  border-radius: 10px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  overflow-y: auto;
  max-height: 80vh;
}

.add-tool-dialog h3 {
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
.form-group textarea,
.form-group select {
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

.preview-image {
  margin-top: 10px;
  text-align: center;
}

.preview-image img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
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

.pagination {
  margin-top: 20px;
  text-align: center;
}

.pagination button {
  margin: 0 5px;
  padding: 6px 12px;
}

/* 借用提示样式 */
.pending-applications {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.application-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 8px;
  border: 1px solid #faad14;
  border-radius: 4px;
  background: #fffbe6;
}

.application-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.application-status {
  padding: 2px 6px;
  background: #faad14;
  color: white;
  border-radius: 10px;
  font-size: 10px;
  font-weight: bold;
}

.borrower {
  color: #666;
}

.application-actions {
  display: flex;
  gap: 5px;
  justify-content: flex-end;
}

.btn-approve {
  padding: 3px 8px;
  background: #52c41a;
  color: white;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
}

.btn-reject {
  padding: 3px 8px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
}

.borrowing-status {
  color: #1890ff;
  font-weight: bold;
}

.no-applications {
  color: #999;
  font-style: italic;
}

.return-confirmations {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.return-confirmations .application-item {
  border: 1px solid #1890ff;
  background: #f0f9ff;
}

.return-confirmations .application-status {
  background: #1890ff;
}
</style>