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
                'status-maintenance': tool.status === 'maintenance'
              }"
            >
              {{ statusText[tool.status] }}
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
  maintenance: '维护中'
}

// API基础URL
const API_BASE_URL = 'http://localhost:8084/api'

// 当前登录用户ID（模拟，实际应该从登录状态获取）
const currentUserId = ref(1)

// 数据状态
const rawData = ref([])
const filter = ref({ toolName: '', status: '' })
const sort = ref({ prop: null, order: null })
const pagination = ref({ currentPage: 1, pageSize: 5 })

// 表单状态
const isFormVisible = ref(false)
const formTitle = ref('发布新工具')
const formData = ref({
  id: null,
  toolName: '',
  toolType: '',
  description: '',
  location: '',
  status: 'available',
  borrowDaysLimit: 7, // 默认最大借用天数为7天
  ownerId: currentUserId.value,
  publishTime: new Date().toISOString()
})

// 计算属性
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
    rawData.value = await response.json()
  } catch (error) {
    console.error('获取工具列表出错:', error)
    alert('获取工具列表失败，请重试')
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
          'Content-Type': 'application/json'
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
    const response = await fetch(`${API_BASE_URL}/published-tools/${id}`, {
      method: 'DELETE'
    })

    if (!response.ok) {
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
  alert('功能待实现：导出CSV')
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
</style>