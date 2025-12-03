<!-- src/views/MyPublished.vue -->
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
                'status-maintenance': tool.status === 'maintenance'
              }"
            >
              {{ statusText[tool.status] }}
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
  maintenance: '维护中'
}

// 当前登录用户ID
const currentUserId = parseInt(localStorage.getItem('userId') || '1')

// 数据状态
const rawData = ref([])
const filter = ref({ toolName: '', status: '' })
const sort = ref({ prop: null, order: null })
const pagination = ref({ currentPage: 1, pageSize: 5 })

// 变量：控制对话框显示
const showAddToolDialog = ref(false)
const newTool = ref({
  // 🔹 修复：删除 toolType 字段
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

const refreshData = async () => {
  try {
    const response = await axios.get(`/api/published-tools/owner/${currentUserId}`)
    rawData.value = response.data
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
</style>