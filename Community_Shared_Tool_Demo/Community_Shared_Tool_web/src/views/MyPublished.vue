<!-- views/MyPublished.vue -->
<template>
  <div class="my-published">
    <div class="operation-buttons">
      <button @click="refreshData">刷新</button>
      <button @click="exportPublishedList">导出发布记录</button>
      <button @click="openToolForm()" class="btn-primary">发布新工具</button>
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
          <th>类型</th>
          <th>当前位置</th>
          <th>状态</th>
          <th>借用提示</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(tool, index) in paginatedData" :key="tool.id">
          <td>{{ formatDate(tool.publishTime) }}</td>
          <td>{{ tool.toolName }}</td>
          <td>{{ tool.toolType }}</td>
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
            <button @click="removeTool(tool)" class="btn-delete">下架</button>
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

    <!-- 工具表单弹窗 -->
    <div v-if="isFormVisible" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ formTitle }}</h3>
          <button @click="closeToolForm" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveTool">
            <div class="form-group">
              <label for="form-toolName">工具名称</label>
              <input v-model="formData.toolName" id="form-toolName" required />
            </div>
            <div class="form-group">
              <label for="form-toolType">工具类型</label>
              <input v-model="formData.toolType" id="form-toolType" required />
            </div>
            <div class="form-group">
              <label for="form-description">描述</label>
              <textarea v-model="formData.description" id="form-description" rows="3"></textarea>
            </div>
            <div class="form-group">
              <label for="form-location">位置</label>
              <input v-model="formData.location" id="form-location" required />
            </div>
            <div class="form-group">
              <label for="form-status">状态</label>
              <select v-model="formData.status" id="form-status">
                <option value="available">可借用</option>
                <option value="maintenance">维护中</option>
              </select>
            </div>
            <div class="form-group">
              <label for="form-borrowDaysLimit">最大借用天数</label>
              <input v-model.number="formData.borrowDaysLimit" type="number" id="form-borrowDaysLimit" min="1" required />
            </div>
            <div class="form-group">
              <label>工具图片</label>
              <div class="image-upload-container">
                <input 
                  type="file" 
                  id="tool-image" 
                  accept="image/*" 
                  @change="handleImageUpload" 
                  style="display: none"
                />
                <label for="tool-image" class="image-upload-btn">
                  <span v-if="!formData.imageUrl">选择图片</span>
                  <span v-else>更换图片</span>
                </label>
                <div v-if="formData.imageUrl" class="image-preview">
                  <img :src="formData.imageUrl" alt="工具图片预览" />
                  <button type="button" @click="removeImage" class="remove-image-btn">删除</button>
                </div>
                <div v-if="uploading" class="upload-progress">
                  <span>上传中...</span>
                </div>
              </div>
            </div>
            <div class="form-actions">
              <button type="submit" class="btn-primary">保存</button>
              <button type="button" @click="closeToolForm">取消</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const statusText = {
  available: '可借用',
  borrowed: '已借出',
  maintenance: '维护中',
  pending: '申请中'
}

// API基础URL
const API_BASE_URL = 'http://localhost:8084/api'

// 当前登录用户ID（从localStorage动态获取）
const currentUserId = ref(parseInt(localStorage.getItem('userId')) || 1)

// 数据状态
const rawData = ref([])
const pendingApplications = ref({})
const waitingReturnConfirmations = ref({})
const filter = ref({ toolName: '', status: '' })
const appliedFilter = ref({ toolName: '', status: '' })
const sort = ref({ prop: null, order: null })
const pagination = ref({ currentPage: 1, pageSize: 5 })

// 表单状态
const isFormVisible = ref(false)
const formTitle = ref('发布新工具')
const uploading = ref(false)
const formData = ref({
  id: null,
  toolName: '',
  toolType: '',
  description: '',
  location: '',
  status: 'available',
  borrowDaysLimit: 7, // 默认最大借用天数为7天
  imageUrl: '',
  ownerId: currentUserId.value,
  publishTime: new Date().toISOString()
})

// 计算属性
const filteredData = computed(() => {
  return rawData.value.filter(item => {
    const nameMatch = item.toolName.includes(appliedFilter.value.toolName)
    const statusMatch = appliedFilter.value.status ? item.status === appliedFilter.value.status : true
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

// 工具函数
const formatDate = (isoStr) => {
  return new Date(isoStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// API请求函数
const fetchPublishedTools = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/published-tools/owner/${currentUserId.value}`)
    if (!response.ok) {
      throw new Error('获取工具列表失败')
    }
    const result = await response.json()
    
    // 处理后端返回的新响应格式
    rawData.value = result.success ? result.data : []
    
    // 获取借用申请
    await fetchPendingApplications()
    // 获取等待归还确认记录
    await fetchWaitingReturnConfirmations()
  } catch (error) {
    console.error('获取工具列表出错:', error)
    alert('获取工具列表失败，请重试')
  }
}

// 获取待处理的借用申请
const fetchPendingApplications = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/borrow/my-applications/${currentUserId.value}`)
    if (!response.ok) {
      throw new Error('获取借用申请失败')
    }
    const result = await response.json()
    
    // 处理后端返回的新响应格式
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
    const response = await fetch(`${API_BASE_URL}/borrow/my-applications/${currentUserId.value}`)
    if (!response.ok) {
      throw new Error('获取借用记录失败')
    }
    const result = await response.json()
    
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
    const response = await fetch(`${API_BASE_URL}/borrow/approve/${applicationId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      const errorText = await response.text()
      console.error('同意借用申请响应错误:', errorText)
      throw new Error('同意借用申请失败')
    }
    
    const result = await response.json()
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
    const response = await fetch(`${API_BASE_URL}/borrow/reject/${applicationId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ rejectReason })
    })
    
    if (!response.ok) {
      throw new Error('拒绝借用申请失败')
    }
    
    const result = await response.json()
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
    const response = await fetch(`${API_BASE_URL}/borrow/confirm-return/${applicationId}`, {
      method: 'POST'
    })
    
    if (!response.ok) {
      throw new Error('确认归还失败')
    }
    
    const result = await response.json()
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
    const response = await fetch(`${API_BASE_URL}/borrow/reject-return/${applicationId}`, {
      method: 'POST'
    })
    
    if (!response.ok) {
      throw new Error('拒绝归还失败')
    }
    
    const result = await response.json()
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

const saveTool = async () => {
  try {
    let response
    if (formData.value.id) {
      // 更新工具
      response = await fetch(`${API_BASE_URL}/published-tools/${formData.value.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': currentUserId.value.toString()
        },
        body: JSON.stringify(formData.value)
      })
    } else {
      // 发布新工具
      response = await fetch(`${API_BASE_URL}/published-tools`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData.value)
      })
    }

    if (!response.ok) {
      throw new Error('保存工具失败')
    }

    closeToolForm()
    await fetchPublishedTools()
    alert('保存成功！')
  } catch (error) {
    console.error('保存工具出错:', error)
    alert('保存失败，请重试')
  }
}

const deleteTool = async (id) => {
  try {
    // 确保currentUserId有值
    if (!currentUserId.value) {
      throw new Error('用户未登录')
    }
    
    const response = await fetch(`${API_BASE_URL}/published-tools/${id}`, {
      method: 'DELETE',
      headers: {
        'X-User-Id': currentUserId.value.toString(),
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      const errorText = await response.text()
      console.error('删除工具响应错误:', errorText)
      throw new Error('删除工具失败')
    }

    await fetchPublishedTools()
    alert('删除成功！')
  } catch (error) {
    console.error('删除工具出错:', error)
    alert('删除失败，请重试')
  }
}

// 页面操作函数
const refreshData = async () => {
  await fetchPublishedTools()
  await fetchWaitingReturnConfirmations()
}

const applyFilter = () => {
  appliedFilter.value = { ...filter.value }
  pagination.value.currentPage = 1
}

const resetFilter = () => {
  filter.value = { toolName: '', status: '' }
  appliedFilter.value = { toolName: '', status: '' }
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
  if (rawData.value.length === 0) {
    alert('没有数据可导出')
    return
  }
  
  // CSV表头
  const headers = ['发布时间', '工具名称', '工具类型', '当前位置', '状态', '描述', '最大借用天数']
  
  // 构建CSV内容
  const csvContent = [
    headers.join(','), // 表头
    ...rawData.value.map(tool => [
      formatDate(tool.publishTime),
      `"${tool.toolName}"`,
      `"${tool.toolType || ''}"`,
      `"${tool.location || ''}"`,
      `"${statusText[tool.status] || ''}"`,
      `"${tool.description || ''}"`,
      tool.borrowDaysLimit || ''
    ].join(','))
  ].join('\n')
  
  // 创建Blob对象并下载
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  
  link.setAttribute('href', url)
  link.setAttribute('download', `发布记录_${new Date().toISOString().split('T')[0]}.csv`)
  link.style.visibility = 'hidden'
  
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  alert('导出成功！')
}

const openToolForm = (tool = null) => {
  if (tool) {
    formTitle.value = '编辑工具'
    formData.value = { ...tool }
  } else {
    formTitle.value = '发布新工具'
    formData.value = {
      id: null,
      toolName: '',
      toolType: '',
      description: '',
      location: '',
      status: 'available',
      borrowDaysLimit: 7, // 默认最大借用天数为7天
      ownerId: currentUserId.value,
      publishTime: new Date().toISOString()
    }
  }
  isFormVisible.value = true
}

const closeToolForm = () => {
  isFormVisible.value = false
}

const editTool = (tool) => {
  openToolForm(tool)
}

const removeTool = (tool) => {
  if (confirm(`确定下架【${tool.toolName}】？`)) {
    deleteTool(tool.id)
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= maxPage.value) {
    pagination.value.currentPage = page
  }
}

// 图片上传处理函数
const handleImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }

  // 检查文件大小（限制为5MB）
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB')
    return
  }

  uploading.value = true
  
  try {
    // 创建FormData对象
    const uploadFormData = new FormData()
    uploadFormData.append('image', file)
    
    // 上传图片到后端
    const response = await fetch(`${API_BASE_URL}/upload/image`, {
      method: 'POST',
      body: uploadFormData
    })
    
    if (!response.ok) {
      throw new Error('图片上传失败')
    }
    
    const result = await response.json()
    formData.value.imageUrl = result.imageUrl
    
  } catch (error) {
    console.error('图片上传出错:', error)
    alert('图片上传失败，请重试')
  } finally {
    uploading.value = false
    // 重置文件输入
    event.target.value = ''
  }
}

const removeImage = () => {
  formData.value.imageUrl = ''
}

// 组件挂载时加载数据
onMounted(() => {
  fetchPublishedTools()
})
</script>

<style scoped>
.my-published {
  padding: 20px;
}

.btn-primary {
  background: #000000 !important;
  color: white !important;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

.btn-edit {
  background: #ffc107;
  color: #212529;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  margin-right: 5px;
  cursor: pointer;
}

.btn-delete {
  background: #dc3545;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
}

/* 复用 MyBorrow 的样式 */
.operation-buttons button:not(.btn-primary),
.filter-form button {
  margin-right: 10px;
  padding: 6px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background: #f8f9fa;
  cursor: pointer;
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
}

.status-available { color: #28a745; }
.status-borrowed { color: #17a2b8; }
.status-maintenance { color: #ffc107; }

.pagination {
  margin-top: 20px;
  text-align: center;
}

.pagination button {
  margin: 0 5px;
  padding: 6px 12px;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-actions {
  margin-top: 20px;
  text-align: right;
}

.form-actions button {
  margin-left: 10px;
  padding: 8px 16px;
  border: 1px solid #ccc;
  border-radius: 4px;
  cursor: pointer;
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

/* 图片上传样式 */
.image-upload-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.image-upload-btn {
  display: inline-block;
  padding: 8px 16px;
  background: #f8f9fa;
  border: 2px dashed #ccc;
  border-radius: 4px;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s ease;
}

.image-upload-btn:hover {
  background: #e9ecef;
  border-color: #007bff;
}

.image-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.image-preview img {
  max-width: 100px;
  max-height: 100px;
  border-radius: 4px;
  object-fit: cover;
}

.remove-image-btn {
  padding: 4px 8px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.remove-image-btn:hover {
  background: #c82333;
}

.upload-progress {
  color: #007bff;
  font-style: italic;
}
</style>