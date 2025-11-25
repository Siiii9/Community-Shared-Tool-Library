<!-- views/MyPublished.vue -->
<template>
  <div class="my-published">
    <div class="operation-buttons">
      <button @click="refreshData">刷新</button>
      <button @click="exportPublishedList">导出发布记录</button>
      <button @click="publishNewTool" class="btn-primary">发布新工具</button>
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
        <tr v-for="(tool, index) in paginatedData" :key="index">
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const statusText = {
  available: '可借用',
  borrowed: '已借出',
  maintenance: '维护中'
}

const generateMockPublished = () => {
  const tools = ['冲击钻', '万用表', '热风枪', '游标卡尺', '示波器']
  const types = ['电动工具', '电子仪器', '测量工具']
  const locations = ['A栋101', 'B栋205', '共享工具柜1', '实验室东侧']
  return Array.from({ length: 25 }, (_, i) => ({
    id: i + 1,
    toolName: tools[i % tools.length],
    toolType: types[i % types.length],
    location: locations[i % locations.length],
    publishTime: new Date(Date.now() - i * 86400000).toISOString(),
    status: ['available', 'borrowed', 'maintenance'][Math.floor(Math.random() * 3)]
  }))
}

const rawData = ref([])
const filter = ref({ toolName: '', status: '' })
const sort = ref({ prop: null, order: null })
const pagination = ref({ currentPage: 1, pageSize: 5 })

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
  return new Date(isoStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const refreshData = () => {
  rawData.value = generateMockPublished()
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

const publishNewTool = () => {
  alert('跳转到发布工具表单')
}

const editTool = (tool) => {
  alert(`编辑工具：${tool.toolName}`)
}

const removeTool = (tool) => {
  if (confirm(`确定下架【${tool.toolName}】？`)) {
    alert('下架成功！')
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

.btn-primary {
  background: #007bff;
  color: white;
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
.operation-buttons button,
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
</style>