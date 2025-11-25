<!-- src/views/Credit.vue -->
<template>
  <div class="credit">
    <div class="credit-header">
      <h1>信用与押金</h1>
      <p>查看您的信用分数和押金状态</p>
    </div>

    <div class="credit-content">
      <!-- 信用分数卡片 -->
      <div class="credit-score-card">
        <div class="score-main">
          <div class="score-circle" :style="scoreCircleStyle">
            <div class="score-value">{{ creditInfo.creditScore }}</div>
            <div class="score-label">信用分数</div>
          </div>
          <div class="score-details">
            <h3>{{ creditLevel }}</h3>
            <p>信用分越高，押金越低，借用更便捷！</p>
            <div class="score-actions">
              <!-- 🔹 新增：两个按钮并排 -->
              <button class="btn-simulate" @click="simulateOverdue">
                模拟逾期1天（-5分）
              </button>
              <button class="btn-restore" @click="restoreCredit">
                恢复信用（+5分）
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 押金信息 -->
      <div class="deposit-card">
        <h3>押金状态</h3>
        <div class="deposit-info">
          <div class="deposit-item">
            <div class="deposit-label">当前押金</div>
            <div class="deposit-value">{{ formattedDeposit }}</div>
            <div class="deposit-desc">{{ depositDesc }}</div>
          </div>
          <div class="deposit-item">
            <div class="deposit-label">押金状态</div>
            <div class="deposit-value" :class="{ paid: creditInfo.isDepositPaid, unpaid: !creditInfo.isDepositPaid }">
              {{ creditInfo.isDepositPaid ? '已缴纳' : '未缴纳' }}
            </div>
            <div class="deposit-desc">借用前需缴纳押金</div>
          </div>
        </div>
        <div class="deposit-actions">
          <button 
            class="btn-primary" 
            @click="payDeposit" 
            :disabled="creditInfo.isDepositPaid"
          >
            {{ creditInfo.isDepositPaid ? '押金已缴纳' : '缴纳押金' }}
          </button>
        </div>
      </div>

      <!-- 信用历史 -->
      <div class="history-card">
        <h3>信用历史记录</h3>
        <div class="history-list">
          <div v-for="record in creditLogs" :key="record.id" class="history-item">
            <div class="history-icon" :class="record.changeScore > 0 ? 'positive' : 'negative'">
              <span class="material-icons">
                {{ record.changeScore > 0 ? 'thumb_up' : 'schedule' }}
              </span>
            </div>
            <div class="history-content">
              <div class="history-title">{{ record.reason }}</div>
              <div class="history-time">{{ formatDate(record.createTime) }}</div>
            </div>
            <div class="history-change" :class="record.changeScore > 0 ? 'positive' : 'negative'">
              {{ record.changeScore > 0 ? '+' : '' }}{{ record.changeScore }}
            </div>
          </div>
          <div v-if="creditLogs.length === 0" class="no-records">
            暂无信用记录
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const creditInfo = ref({
  creditScore: 100,
  depositAmount: 200.00,
  isDepositPaid: false
})

const creditLogs = ref([])
const userId = 1 // 演示用固定用户ID（实际项目应从登录状态获取）

// 获取信用信息
const fetchCreditInfo = async () => {
  try {
    const res = await axios.get('/api/credit/info', { params: { userId } })
    creditInfo.value = res.data
  } catch (error) {
    console.error('获取信用信息失败:', error)
  }
}

// 获取信用日志
const fetchCreditLogs = async () => {
  try {
    const res = await axios.get('/api/credit/logs', { params: { userId } })
    creditLogs.value = res.data
  } catch (error) {
    console.error('获取信用日志失败:', error)
  }
}

// 模拟逾期1天（-5分）
const simulateOverdue = async () => {
  if (!confirm('确定模拟逾期1天？信用分将减少5分')) return
  try {
    await axios.post('/api/credit/simulate-overdue', null, { params: { userId } })
    await fetchCreditInfo()
    await fetchCreditLogs()
  } catch (error) {
    alert('模拟逾期失败，请重试')
  }
}

// 🔹 新增：恢复信用（+5分）
const restoreCredit = async () => {
  if (!confirm('确定恢复信用？信用分将增加5分')) return
  try {
    await axios.post('/api/credit/restore-credit', null, { params: { userId } })
    await fetchCreditInfo()
    await fetchCreditLogs()
  } catch (error) {
    alert('恢复信用失败，请重试')
  }
}

// 缴纳押金（演示）
const payDeposit = async () => {
  if (!confirm('确认缴纳押金？')) return
  try {
    await axios.post('/api/credit/pay-deposit', null, { params: { userId } })
    creditInfo.value.isDepositPaid = true
    alert('押金缴纳成功！')
  } catch (error) {
    alert('押金缴纳失败')
  }
}

// 格式化押金金额
const formattedDeposit = computed(() => {
  return `¥${creditInfo.value.depositAmount?.toFixed(2) || '0.00'}`
})

// 押金描述
const depositDesc = computed(() => {
  const score = creditInfo.value.creditScore
  if (score >= 90) return 'AAA级用户，押金5折！'
  if (score >= 80) return 'AA级用户，押金7折！'
  if (score >= 70) return 'A级用户，标准押金'
  return '信用较低，押金1.5倍'
})

// 信用等级
const creditLevel = computed(() => {
  const score = creditInfo.value.creditScore
  if (score >= 90) return '信用优秀 (AAA)'
  if (score >= 80) return '信用良好 (AA)'
  if (score >= 70) return '信用一般 (A)'
  return '信用较差 (B)'
})

// 动态圆环样式
const scoreCircleStyle = computed(() => {
  const score = creditInfo.value.creditScore || 0
  const percentage = Math.min(100, Math.max(0, score))
  return {
    background: `conic-gradient(#27ae60 0% ${percentage}%, #ecf0f1 ${percentage}% 100%)`
  }
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(async () => {
  await fetchCreditInfo()
  await fetchCreditLogs()
})
</script>

<style scoped>
.credit {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.credit-header {
  margin-bottom: 30px;
}

.credit-header h1 {
  color: #2c3e50;
  margin-bottom: 8px;
}

.credit-header p {
  color: #7f8c8d;
  font-size: 1.1rem;
}

.credit-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.credit-score-card {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.score-main {
  display: flex;
  align-items: center;
  gap: 40px;
}

.score-circle {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.score-circle::before {
  content: '';
  position: absolute;
  width: 120px;
  height: 120px;
  background: white;
  border-radius: 50%;
}

.score-value {
  font-size: 2.5rem;
  font-weight: bold;
  color: #2c3e50;
  z-index: 1;
}

.score-label {
  color: #7f8c8d;
  font-size: 0.9rem;
  z-index: 1;
}

.score-details h3 {
  color: #27ae60;
  margin-bottom: 10px;
}

.score-details p {
  color: #7f8c8d;
  margin-bottom: 20px;
}

/* 🔹 修改：按钮并排 */
.score-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.btn-simulate {
  padding: 8px 16px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.btn-simulate:hover {
  background: #c0392b;
}

/* 🔹 新增：恢复信用按钮 */
.btn-restore {
  padding: 8px 16px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.btn-restore:hover {
  background: #219653;
}

.deposit-card, .history-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.deposit-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin: 20px 0;
}

.deposit-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.deposit-label {
  color: #7f8c8d;
  margin-bottom: 10px;
}

.deposit-value {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 5px;
}

.deposit-value.paid {
  color: #27ae60;
}

.deposit-value.unpaid {
  color: #e74c3c;
}

.deposit-desc {
  color: #95a5a6;
  font-size: 0.9rem;
}

.deposit-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.history-list {
  margin-top: 20px;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ecf0f1;
  gap: 15px;
}

.history-item:last-child {
  border-bottom: none;
}

.history-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.history-icon.positive {
  background: #d4edda;
  color: #155724;
}

.history-icon.negative {
  background: #f8d7da;
  color: #721c24;
}

.history-content {
  flex: 1;
}

.history-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.history-time {
  color: #95a5a6;
  font-size: 0.8rem;
}

.history-change {
  font-weight: bold;
  font-size: 1.1rem;
}

.history-change.positive {
  color: #27ae60;
}

.history-change.negative {
  color: #e74c3c;
}

.btn-primary {
  padding: 12px 24px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.btn-primary:hover {
  background: #2980b9;
}

.no-records {
  text-align: center;
  color: #95a5a6;
  padding: 20px;
}
</style>