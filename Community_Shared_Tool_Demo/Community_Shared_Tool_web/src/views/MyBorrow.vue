<!-- views/MyBorrow.vue -->
<template>
  <div class="my-borrow">
    <div class="operation-buttons">
      <button @click="refreshData">刷新</button>
      <button @click="exportBorrowList">导出借用记录</button>
    </div>

    <div class="filter-form">
      <label for="toolName">工具名称</label>
      <input v-model="filter.toolName" id="toolName" placeholder="输入工具名称" />

      <label for="borrowStatus">借用状态</label>
      <select v-model="filter.status" id="borrowStatus">
        <option value="">全部状态</option>
        <option value="borrowing">借用中</option>
        <option value="returned">已归还</option>
        <option value="overdue">已逾期</option>
      </select>

      <button @click="applyFilter">筛选</button>
      <button @click="resetFilter">重置</button>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th @click="sortData('borrowTime')">借用时间</th>
          <th>工具名称</th>
          <th>工具类型</th>
          <th>预计归还</th>
          <th>实际归还</th>
          <th @click="sortData('status')">状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(record, index) in paginatedData" :key="record.id">
          <td>{{ formatDate(record.borrowTime) }}</td>
          <td>{{ record.toolName }}</td>
          <td>{{ record.toolType }}</td>
          <td>{{ formatDate(record.expectedReturnTime) }}</td>
          <td>{{ record.actualReturnTime ? formatDate(record.actualReturnTime) : '—' }}</td>
          <td>
            <span
              :class="{
                'status-borrowing': record.status === 'borrowing',
                'status-returned': record.status === 'returned',
                'status-overdue': record.status === 'overdue'
              }"
            >
              {{ statusText[record.status] }}
            </span>
          </td>
          <td>
            <button v-if="record.status === 'borrowing'" @click="handleReturn(record)" class="btn-return">归还</button>
            <span v-else-if="record.status === 'returned'" class="status-returned">已归还</span>
            <span v-else-if="record.status === 'overdue'" class="status-overdue">已逾期</span>
            <span v-else>—</span>
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
import axios from 'axios'

// 状态映射
const statusText = {
  borrowing: '借用中',
  returned: '已归还',
  overdue: '已逾期'
}

// API基础URL
const API_BASE_URL = 'http://localhost:8080/api'

// 当前登录用户ID（模拟，实际应该从登录状态获取）
const currentUserId = ref(1)

// 数据状态
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
    // 调用新的BorrowInfo API获取借用记录
    const response = await axios.get(`${API_BASE_URL}/borrow-infos/borrower/${currentUserId.value}`)
    rawData.value = response.data.map(record => ({
      id: record.id,
      toolName: record.toolName,
      toolType: record.toolType,
      borrowTime: record.borrowTime,
      expectedReturnTime: record.expectedReturnTime,
      actualReturnTime: record.actualReturnTime,
      status: record.status,
      borrowRecord: record
    }))
  } catch (error) {
    console.error('获取借用记录失败：', error)
    alert('获取借用记录失败，请重试')
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

const exportBorrowList = () => {
  const csvContent = [
    '借用时间,工具名称,工具类型,预计归还,实际归还,状态',
    ...sortedData.value.map(item =>
      `"${formatDate(item.borrowTime)}","${item.toolName}","${item.toolType}","${formatDate(item.expectedReturnTime)}","${formatDate(item.actualReturnTime)}","${statusText[item.status]}"`
    )
  ].join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `my_borrow_records_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
}

// 归还工具
const handleReturn = async (record) => {
  if (confirm(`确定归还工具【${record.toolName}】？`)) {
    try {
      const response = await axios.patch(`${API_BASE_URL}/borrow-infos/${record.id}/return`)
      if (response.status === 200) {
        alert('归还成功！')
        await refreshData()
      }
    } catch (error) {
      console.error('归还失败：', error)
      alert('归还失败，请重试')
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
.my-borrow {
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

.status-borrowing {
  background: #1890ff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-returned {
  background: #722ed1;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-overdue {
  background: #ff4d4f;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.btn-take {
  background: #1890ff;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-take:hover {
  background: #40a9ff;
}

.btn-return {
  padding: 4px 8px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
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