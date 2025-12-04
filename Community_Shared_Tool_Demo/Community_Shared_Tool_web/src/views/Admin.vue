<template>
  <div class="admin-container">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">
          <span class="stat-symbol">Total</span>
        </div>
        <div class="stat-info">
          <h3>{{ stats.totalUsers || 0 }}</h3>
          <p>总用户数</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon active">
          <span class="stat-symbol">Active</span>
        </div>
        <div class="stat-info">
          <h3>{{ stats.activeUsers || 0 }}</h3>
          <p>活跃用户</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon frozen">
          <span class="stat-symbol">Frozen</span>
        </div>
        <div class="stat-info">
          <h3>{{ stats.frozenUsers || 0 }}</h3>
          <p>冻结用户</p>
        </div>
      </div>
    </div>

    <!-- 用户表格 -->
    <div class="users-table">
      <div class="table-header">
        <h2>用户管理</h2>
        <div class="header-actions">
          <div class="search-box">
            <input 
              type="text" 
              v-model="searchKeyword" 
              placeholder="搜索用户名或基本信息..."
              @input="filterUsers"
            />
            <span class="material-icons">search</span>
          </div>
          <el-button 
            @click="refreshData" 
            type="primary" 
            :loading="refreshing"
            icon="refresh"
          >
            刷新
          </el-button>
        </div>
      </div>

      <el-table :data="filteredUsers" style="width: 100%" v-loading="loading" border>
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="userBasicinfo" label="基本信息" min-width="150" />
        <el-table-column prop="creditScore" label="信誉分" width="100">
          <template #default="scope">
            <el-tag :type="getCreditTagType(scope.row.creditScore)">
              {{ scope.row.creditScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="depositAmount" label="押金" width="100">
          <template #default="scope">
            ¥{{ scope.row.depositAmount || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="isDepositPaid" label="押金状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isDepositPaid ? 'success' : 'warning'">
              {{ scope.row.isDepositPaid ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isFrozen" label="账户状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isFrozen ? 'danger' : 'success'">
              {{ scope.row.isFrozen ? '已冻结' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.isFrozen"
              @click="unfreezeUser(scope.row.userId)"
              type="success"
              size="small"
              icon="unlock"
            >解封</el-button>
            <el-button 
              v-else
              @click="freezeUser(scope.row.userId)"
              type="warning"
              size="small"
              icon="lock"
              class="center-text-btn"
            >冻结</el-button>
            <el-button 
              @click="resetCredit(scope.row.userId)"
              type="primary"
              size="small"
              icon="refresh"
              class="center-text-btn"
            >重置信誉</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const users = ref([])
const filteredUsers = ref([])
const searchKeyword = ref('')
const loading = ref(false)
const refreshing = ref(false)

const stats = ref({
  totalUsers: 0,
  activeUsers: 0,
  frozenUsers: 0
})

// 刷新数据
const refreshData = async () => {
  refreshing.value = true
  try {
    await fetchUsersAndStats()
  } finally {
    refreshing.value = false
  }
}

// 同时获取用户列表和统计数据
const fetchUsersAndStats = async () => {
  try {
    await Promise.all([fetchUsersData(), fetchStatsData()])
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

// 获取用户列表数据
const fetchUsersData = async () => {
  try {
    const username = localStorage.getItem('username')
    console.log('获取用户列表，当前用户:', username)
    
    const response = await axios.get('/api/admin/users', {
      params: {
        currentAdmin: username
      },
      timeout: 10000
    })
    
    console.log('用户列表响应:', response.data)
    users.value = response.data
    filteredUsers.value = response.data
    return true
  } catch (error) {
    console.error('获取用户列表失败:', error)
    handleUsersError(error)
    return false
  }
}

// 获取统计数据
const fetchStatsData = async () => {
  try {
    const response = await axios.get('/api/admin/stats', { timeout: 5000 })
    console.log('统计数据响应:', response.data)
    
    if (response.data.success !== false) {
      stats.value = response.data
      return true
    } else {
      ElMessage.warning('获取统计数据失败: ' + (response.data.error || '未知错误'))
      return false
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 不显示错误，因为主要数据已加载
    return false
  }
}

// 处理用户列表错误
const handleUsersError = (error) => {
  if (error.code === 'ECONNABORTED') {
    ElMessage.error('请求超时，请检查网络连接')
  } else if (error.response) {
    console.error('错误响应状态:', error.response.status)
    console.error('错误响应数据:', error.response.data)
    
    if (error.response.status === 401 || error.response.status === 403) {
      ElMessage.warning('权限不足，请重新登录')
      router.push('/login')
    } else if (error.response.status === 500) {
      ElMessage.error('服务器错误: ' + (error.response.data?.message || '未知错误'))
    } else {
      ElMessage.error('获取用户列表失败: ' + (error.response.data?.message || '请求错误'))
    }
  } else if (error.request) {
    console.error('无响应:', error.request)
    ElMessage.error('网络连接失败，请检查后端服务是否启动')
  } else {
    ElMessage.error('请求配置错误: ' + error.message)
  }
}

// 冻结用户
const freezeUser = async (userId) => {
  try {
    await ElMessageBox.confirm('确定要冻结此用户吗？冻结后用户将无法登录。', '确认冻结', {
      type: 'warning',
      confirmButtonText: '确认冻结',
      cancelButtonText: '取消'
    })
    
    console.log('发送冻结请求，用户ID:', userId)
    const response = await axios.put(`/api/admin/users/${userId}/freeze`)
    console.log('冻结响应:', response.data)
    
    if (response.data.success !== false) {
      ElMessage.success(response.data.message || '用户已冻结')
      // 强制更新统计数据，即使获取用户列表失败
      await forceUpdateStats()
    } else {
      ElMessage.error(response.data.error || '冻结失败')
    }
  } catch (error) {
    console.error('冻结用户失败:', error)
    if (error.response?.status === 400) {
      ElMessage.error(error.response.data || '不能冻结管理员账户')
    } else if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '冻结失败')
    }
  }
}

// 解封用户
const unfreezeUser = async (userId) => {
  try {
    await ElMessageBox.confirm('确定要解封此用户吗？', '确认解封', {
      type: 'success',
      confirmButtonText: '确认解封',
      cancelButtonText: '取消'
    })
    
    console.log('发送解封请求，用户ID:', userId)
    const response = await axios.put(`/api/admin/users/${userId}/unfreeze`)
    console.log('解封响应:', response.data)
    
    if (response.data.success !== false) {
      ElMessage.success(response.data.message || '用户已解封')
      // 强制更新统计数据，即使获取用户列表失败
      await forceUpdateStats()
    } else {
      ElMessage.error(response.data.error || '解封失败')
    }
  } catch (error) {
    console.error('解封用户失败:', error)
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '解封失败')
    }
  }
}

// 强制更新统计数据（即使获取用户列表失败也要更新）
const forceUpdateStats = async () => {
  try {
    console.log('强制更新统计数据...')
    const statsSuccess = await fetchStatsData()
    
    if (statsSuccess) {
      console.log('统计数据更新成功')
    } else {
      console.log('统计数据更新失败，但已尝试')
    }
    
    // 仍然尝试获取用户列表，但不阻塞
    setTimeout(async () => {
      try {
        await fetchUsersData()
      } catch (e) {
        console.log('获取用户列表失败，但统计数据已更新')
      }
    }, 500)
    
  } catch (error) {
    console.error('更新统计数据失败:', error)
  }
}

// 重置信誉分
const resetCredit = async (userId) => {
  try {
    await ElMessageBox.confirm('确定要将此用户的信誉分重置为100吗？', '重置信誉分', {
      type: 'info',
      confirmButtonText: '确认重置',
      cancelButtonText: '取消'
    })
    
    console.log('发送重置信誉请求，用户ID:', userId)
    const response = await axios.put(`/api/admin/users/${userId}/reset-credit`)
    console.log('重置信誉响应:', response.data)
    
    if (response.data.success !== false) {
      ElMessage.success(response.data.message || '信誉分已重置')
      // 延迟1秒后刷新数据
      setTimeout(() => {
        fetchUsersAndStats()
      }, 1000)
    } else {
      ElMessage.error(response.data.error || '重置失败')
    }
  } catch (error) {
    console.error('重置信誉分失败:', error)
    if (error.response?.status === 400) {
      ElMessage.error(error.response.data || '不能重置管理员信誉分')
    } else if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '重置失败')
    }
  }
}

// 根据信誉分获取标签类型
const getCreditTagType = (score) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

// 过滤用户
const filterUsers = () => {
  if (!searchKeyword.value) {
    filteredUsers.value = users.value
    return
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  filteredUsers.value = users.value.filter(user => 
    (user.username && user.username.toLowerCase().includes(keyword)) ||
    (user.userBasicinfo && user.userBasicinfo.toLowerCase().includes(keyword))
  )
}

onMounted(() => {
  // 检查是否是管理员
  const isAdmin = localStorage.getItem('isAdmin') === 'true'
  if (!isAdmin) {
    ElMessage.warning('您不是管理员，无法访问此页面')
    router.push('/tool-map')
    return
  }
  
  loading.value = true
  fetchUsersAndStats().finally(() => {
    loading.value = false
  })
})
</script>

<style scoped>
.admin-container {
  padding: 20px;
  background: #f5f6fa;
  min-height: calc(100vh - 60px);
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.15);
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
  transition: all 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.4);
}

.stat-icon .stat-symbol {
  font-size: 18px;
  color: #ffffff !important;
  font-weight: bold;
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-icon .material-icons {
  transform: scale(1.2);
}

.stat-icon.active {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.3);
}

.stat-card:hover .stat-icon.active {
  box-shadow: 0 6px 20px rgba(46, 204, 113, 0.4);
}

.stat-icon.frozen {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
}

.stat-card:hover .stat-icon.frozen {
  box-shadow: 0 6px 20px rgba(231, 76, 60, 0.4);
}

.stat-info h3 {
  font-size: 32px;
  margin: 0;
  color: #2c3e50;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.stat-info p {
  margin: 8px 0 0;
  color: #7f8c8d;
  font-size: 16px;
  font-weight: 500;
}

.users-table {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.table-header h2 {
  margin: 0;
  color: #2c3e50;
}

.search-box {
  position: relative;
  width: 300px;
}

.search-box input {
  width: 100%;
  padding: 10px 40px 10px 15px;
  border: 1px solid #ddd;
  border-radius: 20px;
  outline: none;
  transition: border-color 0.3s;
}

.search-box input:focus {
  border-color: #3498db;
}

.search-box .material-icons {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #95a5a6;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #f8f9fa;
  color: #2c3e50;
  font-weight: 600;
}

:deep(.el-tag) {
  border: none;
  padding: 4px 8px;
  font-weight: 500;
}

/* Custom styles for freeze/unfreeze buttons */
:deep(.el-button--small) {
  margin-right: 8px;
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

/* Center text for specific buttons */
:deep(.el-button.center-text-btn) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

/* Adjust text position for freeze and reset buttons */
:deep(.el-button--small.el-button--warning),
:deep(.el-button--small.el-button--primary) {
  justify-content: center;
  text-align: center;
  padding-left: 12px;
  padding-right: 12px;
}

:deep(.el-button--small:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

:deep(.el-button--warning.is-small) {
  background: #f39c12;
  border-color: #f39c12;
  color: white;
}

:deep(.el-button--warning.is-small:hover) {
  background: #e67e22;
  border-color: #e67e22;
}

:deep(.el-button--success.is-small) {
  background: #27ae60;
  border-color: #27ae60;
  color: white;
}

:deep(.el-button--success.is-small:hover) {
  background: #229954;
  border-color: #229954;
}

:deep(.el-button--primary.is-small) {
  background: #3498db;
  border-color: #3498db;
  color: white;
}

:deep(.el-button--primary.is-small:hover) {
  background: #2980b9;
  border-color: #2980b9;
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .search-box {
    flex: 1;
  }
}
</style>