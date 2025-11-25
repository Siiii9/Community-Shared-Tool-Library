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
          <div class="score-circle">
            <div class="score-value">95</div>
            <div class="score-label">信用分数</div>
          </div>
          <div class="score-details">
            <h3>信用良好</h3>
            <p>继续保持良好的借用记录可以享受更多优惠</p>
            <div class="score-breakdown">
              <div class="breakdown-item">
                <span class="label">初始分数</span>
                <span class="value">100</span>
              </div>
              <div class="breakdown-item">
                <span class="label">累计加分</span>
                <span class="value positive">+8</span>
              </div>
              <div class="breakdown-item">
                <span class="label">累计扣分</span>
                <span class="value negative">-13</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 押金信息 -->
      <div class="deposit-card">
        <h3>押金状态</h3>
        <div class="deposit-info">
          <div class="deposit-item">
            <div class="deposit-label">当前押金系数</div>
            <div class="deposit-value coefficient">70%</div>
            <div class="deposit-desc">基于您的信用分数，享受押金优惠</div>
          </div>
          <div class="deposit-item">
            <div class="deposit-label">押金余额</div>
            <div class="deposit-value balance">¥200.00</div>
            <div class="deposit-desc">可用于借用工具的押金</div>
          </div>
        </div>
        <div class="deposit-actions">
          <button class="btn-primary" @click="showRecharge = true">充值押金</button>
          <button class="btn-secondary" @click="showWithdraw = true">申请提现</button>
        </div>
      </div>

      <!-- 信用历史 -->
      <div class="history-card">
        <h3>信用历史记录</h3>
        <div class="history-list">
          <div v-for="record in creditHistory" :key="record.id" class="history-item">
            <div class="history-icon" :class="record.type">
              <span class="material-icons">{{ record.icon }}</span>
            </div>
            <div class="history-content">
              <div class="history-title">{{ record.title }}</div>
              <div class="history-desc">{{ record.description }}</div>
              <div class="history-time">{{ record.time }}</div>
            </div>
            <div class="history-change" :class="record.change > 0 ? 'positive' : 'negative'">
              {{ record.change > 0 ? '+' : '' }}{{ record.change }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 充值模态框 -->
    <div v-if="showRecharge" class="modal-overlay" @click="showRecharge = false">
      <div class="modal-content" @click.stop>
        <h3>押金充值</h3>
        <p>演示版本，充值功能暂不可用</p>
        <div class="modal-actions">
          <button class="btn-primary" @click="showRecharge = false">确定</button>
        </div>
      </div>
    </div>

    <!-- 提现模态框 -->
    <div v-if="showWithdraw" class="modal-overlay" @click="showWithdraw = false">
      <div class="modal-content" @click.stop>
        <h3>押金提现</h3>
        <p>演示版本，提现功能暂不可用</p>
        <div class="modal-actions">
          <button class="btn-primary" @click="showWithdraw = false">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showRecharge = ref(false)
const showWithdraw = ref(false)

const creditHistory = ref([
  {
    id: 1,
    type: 'positive',
    icon: 'thumb_up',
    title: '按时归还工具',
    description: '电钻 - 提前2小时归还',
    time: '2024-03-15 14:30',
    change: 2
  },
  {
    id: 2,
    type: 'positive',
    icon: 'star',
    title: '获得好评',
    description: '来自用户张先生的5星评价',
    time: '2024-03-10 09:15',
    change: 1
  },
  {
    id: 3,
    type: 'negative',
    icon: 'schedule',
    title: '归还逾期',
    description: '手电筒 - 逾期1天归还',
    time: '2024-03-05 16:20',
    change: -5
  },
  {
    id: 4,
    type: 'positive',
    icon: 'thumb_up',
    title: '按时归还工具',
    description: '梯子 - 按时归还',
    time: '2024-02-28 11:45',
    change: 1
  }
])
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
  background: conic-gradient(#27ae60 0% 95%, #ecf0f1 95% 100%);
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

.score-breakdown {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.breakdown-item .label {
  color: #7f8c8d;
}

.breakdown-item .value {
  font-weight: 600;
}

.breakdown-item .value.positive {
  color: #27ae60;
}

.breakdown-item .value.negative {
  color: #e74c3c;
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

.deposit-value.coefficient {
  color: #3498db;
}

.deposit-value.balance {
  color: #27ae60;
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

.history-desc {
  color: #7f8c8d;
  font-size: 0.9rem;
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

.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
  max-width: 400px;
  width: 90%;
  text-align: center;
}

.modal-content h3 {
  margin-bottom: 15px;
  color: #2c3e50;
}

.modal-content p {
  color: #7f8c8d;
  margin-bottom: 20px;
}

.modal-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}
</style>