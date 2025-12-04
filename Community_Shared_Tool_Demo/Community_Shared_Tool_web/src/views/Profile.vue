<template>
  <div class="profile">
    <div class="profile-header">
      <h1>个人信息</h1>
      <p>管理您的个人资料和账户设置</p>
    </div>

    <div class="profile-content">
      <div class="profile-card">
        <div class="avatar-section">
          <div class="avatar">
            <span class="material-icons">account_circle</span>
          </div>
          <button class="edit-avatar-btn">更换头像</button>
        </div>

        <div class="info-section">
          <div class="info-item">
            <label>用户名</label>
            <div class="info-value">demo_user</div>
          </div>
          <div class="info-item">
            <label>手机号</label>
            <div class="info-value">138****1234</div>
          </div>
          <div class="info-item">
            <label>住址</label>
            <div class="info-value">北京市海淀区XX小区 8号楼 1203室</div>
          </div>
          <div class="info-item">
            <label>实名状态</label>
            <div class="status-section">
              <div :class="['status-badge', isVerified ? 'verified' : 'unverified']">
                {{ isVerified ? '已认证' : '未认证' }}
              </div>
              <button v-if="!isVerified" class="verify-btn" @click="showVerificationModal = true">实名认证</button>
              <div v-else class="verified-info">
                <span class="material-icons">check_circle</span>
                认证成功
              </div>
            </div>
          </div>
          
          <!-- 认证信息显示 -->
          <div v-if="isVerified" class="verification-details">
            <div class="info-item">
              <label>认证姓名</label>
              <div class="info-value">{{ getVerificationInfo().name }}</div>
            </div>
            <div class="info-item">
              <label>认证年龄</label>
              <div class="info-value">{{ getVerificationInfo().age }}岁</div>
            </div>
            <div class="info-item">
              <label>认证性别</label>
              <div class="info-value">{{ getVerificationInfo().gender === 'male' ? '男' : '女' }}</div>
            </div>
            <div class="info-item">
              <label>身份证号</label>
              <div class="info-value">{{ getVerificationInfo().idCard }}</div>
            </div>
            <div class="info-item">
              <label>认证时间</label>
              <div class="info-value">{{ formatDate(getVerificationInfo().verifiedAt) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="stats-card">
        <h3>使用统计</h3>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-number">12</div>
            <div class="stat-label">成功借用</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">8</div>
            <div class="stat-label">发布工具</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">4.8</div>
            <div class="stat-label">平均评分</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">95</div>
            <div class="stat-label">信用分数</div>
          </div>
        </div>
      </div>
    </div>
  </div>
    <!-- 实名认证弹窗 -->
    <div v-if="showVerificationModal" class="modal-overlay" @click="showVerificationModal = false">
      <div class="verification-modal" @click.stop>
        <div class="modal-header">
          <h3>实名认证</h3>
          <button class="close-btn" @click="showVerificationModal = false">
            <span class="material-icons">close</span>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>姓名</label>
            <input type="text" v-model="verificationData.name" placeholder="请输入真实姓名" />
          </div>
          <div class="form-group">
            <label>年龄</label>
            <input type="number" v-model="verificationData.age" placeholder="请输入年龄" />
          </div>
          <div class="form-group">
            <label>性别</label>
            <div class="gender-options">
              <label>
                <input type="radio" v-model="verificationData.gender" value="male" />
                男
              </label>
              <label>
                <input type="radio" v-model="verificationData.gender" value="female" />
                女
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>身份证号</label>
            <input type="text" v-model="verificationData.idCard" placeholder="请输入身份证号码" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showVerificationModal = false">取消</button>
          <button class="submit-btn" @click="submitVerification">提交认证</button>
        </div>
      </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

// 实名认证状态
const showVerificationModal = ref(false)
const isVerified = ref(false)

// 实名认证数据
const verificationData = reactive({
  name: '',
  age: '',
  gender: '',
  idCard: ''
})

// 页面加载时检查认证状态
onMounted(() => {
  const savedVerification = localStorage.getItem('userVerification')
  if (savedVerification) {
    const verification = JSON.parse(savedVerification)
    isVerified.value = verification.isVerified
  }
})

// 获取认证信息
const getVerificationInfo = () => {
  const savedVerification = localStorage.getItem('userVerification')
  if (savedVerification) {
    return JSON.parse(savedVerification)
  }
  return {
    name: '',
    age: '',
    gender: '',
    idCard: '',
    verifiedAt: ''
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 提交实名认证
const submitVerification = () => {
  // 简单的表单验证
  if (!verificationData.name || !verificationData.age || !verificationData.gender || !verificationData.idCard) {
    alert('请填写完整的认证信息')
    return
  }
  
  // 年龄验证
  if (verificationData.age < 0 || verificationData.age > 150) {
    alert('请输入有效的年龄')
    return
  }
  
  // 身份证号格式简单验证
  if (verificationData.idCard.length < 15 || verificationData.idCard.length > 18) {
    alert('请输入有效的身份证号码')
    return
  }
  
  // 保存认证信息到本地存储
  const verificationInfo = {
    isVerified: true,
    name: verificationData.name,
    age: verificationData.age,
    gender: verificationData.gender,
    idCard: verificationData.idCard,
    verifiedAt: new Date().toISOString()
  }
  
  localStorage.setItem('userVerification', JSON.stringify(verificationInfo))
  
  // 更新认证状态
  isVerified.value = true
  showVerificationModal.value = false
  alert(`实名认证成功！\n姓名：${verificationData.name}\n身份证号：${verificationData.idCard}`)
  
  // 清空表单数据
  Object.assign(verificationData, {
    name: '',
    age: '',
    gender: '',
    idCard: ''
  })
}
</script>

<style scoped>
.profile {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-header {
  text-align: center;
  margin-bottom: 30px;
}

.profile-header h1 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.profile-header p {
  color: #7f8c8d;
}

.profile-content {
  display: grid;
  gap: 20px;
}

.profile-card {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  gap: 40px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar .material-icons {
  font-size: 60px;
  color: #bdc3c7;
}

.edit-avatar-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.info-section {
  flex: 1;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ecf0f1;
}

.info-item label {
  width: 100px;
  font-weight: 600;
  color: #2c3e50;
}

.info-value {
  flex: 1;
  color: #7f8c8d;
}

.status-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
}

.status-badge.verified {
  background: #e8f5e8;
  color: #27ae60;
}

.status-badge.unverified {
  background: #fff3cd;
  color: #856404;
}

.verify-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.verify-btn:hover {
  background: #2980b9;
}

.verified-info {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #27ae60;
  font-size: 0.9rem;
}

.verified-info .material-icons {
  font-size: 1.1rem;
}

/* 认证信息详情样式 */
.verification-details {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #27ae60;
}

.verification-details .info-item {
  border-bottom: none;
  margin-bottom: 10px;
  padding-bottom: 5px;
}

.verification-details .info-item:last-child {
  margin-bottom: 0;
}

.verification-details .info-item label {
  color: #27ae60;
  font-weight: 600;
}

.verification-details .info-value {
  color: #2c3e50;
  font-weight: 500;
}

/* 实名认证弹窗样式 */
.modal-overlay {
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

.verification-modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ecf0f1;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #7f8c8d;
  padding: 5px;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.form-group input[type="text"],
.form-group input[type="number"] {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
}

.gender-options {
  display: flex;
  gap: 20px;
}

.gender-options label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: normal;
  cursor: pointer;
}

.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px solid #ecf0f1;
}

.cancel-btn {
  background: #95a5a6;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
}

.submit-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
}

.cancel-btn:hover {
  background: #7f8c8d;
}

.submit-btn:hover {
  background: #2980b9;
}

.stats-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.stats-card h3 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #3498db;
  margin-bottom: 5px;
}

.stat-label {
  color: #7f8c8d;
  font-size: 0.9rem;
}
</style>