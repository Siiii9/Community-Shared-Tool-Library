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
          <td>{{ formatDate(record.expectedReturnTime) }}</td>
          <td>{{ record.actualReturnTime ? formatDate(record.actualReturnTime) : '—' }}</td>
          <td>
            <span
              :class="{
                'status-borrowing': record.status === 'borrowing',
                'status-returned': record.status === 'returned',
                'status-overdue': record.status === 'overdue',
                'status-waiting': record.status === 'waiting_return_confirm' || record.applicationStatus === 'pending',
                'status-rejected': record.status === 'rejected' || record.applicationStatus === 'rejected'
              }"
            >
              <!-- 显示更详细的状态信息 -->
              <template v-if="record.recordType === 'borrowed'">
                {{ record.applicationStatus === 'pending' ? '等待同意' : statusText[record.status] }}
              </template>
              <template v-else>
                {{ record.applicationStatus === 'pending' ? '等待审批' : 
                   record.status === 'waiting_return_confirm' ? '等待归还确认' : 
                   statusText[record.status] }}
              </template>
            </span>
          </td>
          <td>
            <!-- 自己借用的工具操作 -->
            <template v-if="record.recordType === 'borrowed'">
              <span v-if="record.applicationStatus === 'pending'" class="status-waiting">等待同意</span>
              <button v-else-if="record.status === 'borrowing' || record.status === 'overdue' || record.status === 'TAKEN' || record.status === 'APPROVED'" @click="handleReturn(record)" class="btn-return">归还</button>
              <span v-else-if="record.status === 'returned' || record.status === 'RETURNED'" class="status-returned">已归还</span>
              <span v-else-if="record.status === 'waiting_return_confirm' || record.status === 'WAITING_RETURN_CONFIRM'" class="status-waiting">等待确认</span>
              <span v-else>—</span>
            </template>
            <!-- 借给别人的工具操作 -->
            <template v-else-if="record.recordType === 'lent'">
              <div v-if="record.applicationStatus === 'pending'">
                <button @click="handleApprove(record)" class="btn-approve">同意</button>
                <button @click="handleReject(record)" class="btn-reject">拒绝</button>
              </div>
              <button v-else-if="record.status === 'borrowing' || record.status === 'overdue' || record.status === 'TAKEN'" @click="handleReturn(record)" class="btn-return">归还</button>
              <button v-else-if="record.status === 'waiting_return_confirm' || record.status === 'WAITING_RETURN_CONFIRM'" @click="handleReturnConfirm(record)" class="btn-return-confirm">确认归还</button>
              <span v-else-if="record.status === 'returned' || record.status === 'RETURNED'" class="status-returned">已归还</span>
              <span v-else-if="record.applicationStatus === 'rejected' || record.status === 'REJECTED'" class="status-rejected">已拒绝</span>
              <span v-else-if="record.status === 'APPROVED'" class="status-approved">已同意</span>
              <span v-else>—</span>
            </template>
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
  overdue: '已逾期',
  waiting_return_confirm: '等待归还确认',
  pending: '等待审批',
  rejected: '已拒绝'
}
// API基础URL（使用相对路径）
const API_BASE_URL = '/api'
// 当前登录用户ID（从localStorage动态获取）
const userIdStr = localStorage.getItem('userId');
const currentUserId = ref(userIdStr ? parseInt(userIdStr) : 1);
// 确保currentUserId始终有值，避免页面消失
if (!userIdStr) {
  localStorage.setItem('userId', '1');
  currentUserId.value = 1;
}
// 数据状态
const rawData = ref([])
// 模拟借用记录数据 - 包含借用人、工具所有者和申请状态
const MOCK_BORROW_RECORDS = [
  // user2申请借用user1的工具，已同意
  { id: 1, toolName: '冲击钻', borrowTime: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(), expectedReturnTime: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toISOString(), actualReturnTime: null, status: 'borrowing', userId: 2, toolOwnerId: 1, applicationStatus: 'approved' },
  // user2申请借用user1的工具，等待同意
  { id: 2, toolName: '万用表', borrowTime: null, expectedReturnTime: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toISOString(), actualReturnTime: null, status: 'pending', userId: 2, toolOwnerId: 1, applicationStatus: 'pending' },
  // user1申请借用user2的工具，已同意
  { id: 3, toolName: '电烙铁', borrowTime: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString(), expectedReturnTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(), actualReturnTime: null, status: 'overdue', userId: 1, toolOwnerId: 2, applicationStatus: 'approved' },
  // user1自己的工具被借用记录
  { id: 4, toolName: '角磨机', borrowTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(), expectedReturnTime: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(), actualReturnTime: null, status: 'borrowing', userId: 3, toolOwnerId: 1, applicationStatus: 'approved' },
  // user1已归还的工具
  { id: 5, toolName: '水平仪', borrowTime: new Date(Date.now() - 15 * 24 * 60 * 60 * 1000).toISOString(), expectedReturnTime: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(), actualReturnTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(), status: 'returned', userId: 1, toolOwnerId: 2, applicationStatus: 'approved' }
]
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
      // 获取当前用户相关的所有借用记录：
      // 1. 自己作为借用人的记录（从BorrowInfo接口获取）
      // 2. 自己作为工具所有者的记录（被别人借用）
      const [myBorrowsResponse, myApplicationsResponse] = await Promise.all([
        axios.get(`/api/borrow-infos/borrower/${currentUserId.value}`),
        axios.get(`/api/borrow/my-applications/${currentUserId.value}`)
      ])
      
      const myBorrows = myBorrowsResponse.data.success ? myBorrowsResponse.data.data : []
      const myApplications = myApplicationsResponse.data.success ? myApplicationsResponse.data.data : []
      
      // 合并并处理记录
      const processedBorrows = myBorrows.map(borrow => ({
        id: borrow.id,
        toolName: borrow.toolName || '未知工具',
        borrowTime: borrow.borrowTime,
        expectedReturnTime: borrow.expectedReturnTime,
        actualReturnTime: borrow.actualReturnTime,
        status: borrow.status || borrow.recordStatus,
        userId: borrow.borrowerId,
        toolOwnerId: borrow.ownerId,
        applicationStatus: borrow.applicationStatus || 'approved',
        borrowRecord: borrow,
        // 标识记录类型：自己借用的还是借给别人的
        recordType: 'borrowed'
      }))
      
      const processedApplications = myApplications.map(app => {
        return {
          id: app.id,
          toolName: app.toolName || '未知工具',
          borrowTime: app.borrowTime,
          expectedReturnTime: app.expectedReturnTime,
          actualReturnTime: app.actualReturnTime,
          status: app.status || app.recordStatus,
          userId: app.borrowerId,
          toolOwnerId: app.ownerId,
          applicationStatus: app.status === 'PENDING' ? 'pending' : app.status === 'WAITING_RETURN_CONFIRM' ? 'waiting' : 'approved',
          borrowRecord: app,
          // 标识记录类型：自己借用的还是借给别人的
          recordType: 'lent'
        };
      })
      
      // 合并所有记录
      rawData.value = [...processedBorrows, ...processedApplications]
      
      // 如果没有数据，使用模拟数据作为备选
      if (rawData.value.length === 0) {
        console.warn('后端未返回借用记录，使用模拟数据')
        let records = MOCK_BORROW_RECORDS.filter(record => 
          record.userId === currentUserId.value || record.toolOwnerId === currentUserId.value
        )
        
        rawData.value = records.map(record => ({
          id: record.id,
          toolName: record.toolName,
          borrowTime: record.borrowTime,
          expectedReturnTime: record.expectedReturnTime,
          actualReturnTime: record.actualReturnTime,
          status: record.status,
          userId: record.userId,
          toolOwnerId: record.toolOwnerId,
          applicationStatus: record.applicationStatus,
          borrowRecord: record,
          // 标识记录类型：自己借用的还是借给别人的
          recordType: record.userId === currentUserId.value ? 'borrowed' : 'lent'
        }))
      }
    } catch (error) {
      console.error('获取借用记录失败：', error)
      // 显示友好的错误提示
      const errorMessage = error.response?.data?.message || error.message || '获取借用记录失败，请重试'
      alert(errorMessage)
      
      // 使用模拟数据作为备选
      console.warn('使用模拟借用记录数据')
      let records = MOCK_BORROW_RECORDS.filter(record => 
        record.userId === currentUserId.value || record.toolOwnerId === currentUserId.value
      )
      
      rawData.value = records.map(record => ({
        id: record.id,
        toolName: record.toolName,
        borrowTime: record.borrowTime,
        expectedReturnTime: record.expectedReturnTime,
        actualReturnTime: record.actualReturnTime,
        status: record.status,
        userId: record.userId,
        toolOwnerId: record.toolOwnerId,
        applicationStatus: record.applicationStatus,
        borrowRecord: record,
        // 标识记录类型：自己借用的还是借给别人的
        recordType: record.userId === currentUserId.value ? 'borrowed' : 'lent'
      }))
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
    '借用时间,工具名称,预计归还,实际归还,状态',
    ...sortedData.value.map(item =>
      `"${formatDate(item.borrowTime)}","${item.toolName}","${formatDate(item.expectedReturnTime)}","${formatDate(item.actualReturnTime)}","${statusText[item.status]}"`
    )
  ].join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `my_borrow_records_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
}
// 归还工具（借用人操作）
const handleReturn = async (record) => {
    if (confirm(`确定归还工具【${record.toolName}】？`)) {
      try {
        let response;
        // 根据记录类型调用不同的API接口
        if (record.recordType === 'borrowed') {
          // 自己借用的记录，调用BorrowInfo的归还接口
          response = await axios.patch(`/api/borrow-infos/${record.id}/return`)
        } else {
          // 借给别人的记录，调用BorrowRecord的归还接口
          const borrowRecordId = record.borrowRecord?.id || record.id
          response = await axios.post(`/api/borrow/return/${borrowRecordId}`)
        }
        
        if (response.data.success) {
          alert('归还申请已提交，请等待工具所有者确认！')
          await refreshData()
        } else {
          throw new Error(response.data.message || '归还失败')
        }
      } catch (error) {
        console.error('归还失败：', error)
        const errorMessage = error.response?.data?.message || error.message || '未知错误'
        alert('归还失败，请重试\n错误信息：' + errorMessage)
      }
    }
  }

// 同意借用申请（工具所有者操作）
const handleApprove = async (record) => {
    if (confirm(`确定同意用户借用工具【${record.toolName}】？`)) {
      try {
        // 调用后端API同意借用申请
        const response = await axios.post(`/api/borrow/approve/${record.id}`)
        
        if (response.data.success) {
          alert('已同意借用申请！')
          await refreshData()
        } else {
          throw new Error(response.data.message || '同意失败')
        }
      } catch (error) {
        console.error('同意借用失败：', error)
        const errorMessage = error.response?.data?.message || error.message || '未知错误'
        alert('同意借用失败，请重试\n错误信息：' + errorMessage)
      }
    }
  }

// 拒绝借用申请（工具所有者操作）
const handleReject = async (record) => {
    if (confirm(`确定拒绝用户借用工具【${record.toolName}】？`)) {
      try {
        // 调用后端API拒绝借用申请
        const response = await axios.post(`/api/borrow/reject/${record.id}`, {
          rejectReason: '不符合借用条件'
        })
        
        if (response.data.success) {
          alert('已拒绝借用申请！')
          await refreshData()
        } else {
          throw new Error(response.data.message || '拒绝失败')
        }
      } catch (error) {
        console.error('拒绝借用失败：', error)
        const errorMessage = error.response?.data?.message || error.message || '未知错误'
        alert('拒绝借用失败，请重试\n错误信息：' + errorMessage)
      }
    }
  }

// 确认归还（工具所有者操作）
const handleReturnConfirm = async (record) => {
    if (confirm(`确定确认用户归还工具【${record.toolName}】？`)) {
      try {
        // 调用后端API确认归还
        const response = await axios.post(`/api/borrow/confirm-return/${record.id}`)
        
        if (response.data.success) {
          alert('已确认归还！')
          await refreshData()
        } else {
          throw new Error(response.data.message || '确认失败')
        }
      } catch (error) {
        console.error('确认归还失败：', error)
        const errorMessage = error.response?.data?.message || error.message || '未知错误'
        alert('确认归还失败，请重试\n错误信息：' + errorMessage)
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
.status-waiting {
  background: #faad14;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
}
.status-rejected {
  background: #ff7875;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
}
.btn-approve {
  background: #52c41a;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
}
.btn-reject {
  background: #ff4d4f;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
}
.btn-return-confirm {
  background: #1890ff;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
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