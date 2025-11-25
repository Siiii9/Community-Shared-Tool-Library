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
            <div class="status-badge verified">已认证</div>
          </div>
          <div class="info-item">
            <label>注册时间</label>
            <div class="info-value">2024-01-15</div>
          </div>
        </div>

        <div class="action-buttons">
          <button class="btn-primary">编辑信息</button>
          <button class="btn-secondary">修改密码</button>
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
</template>

<script setup>
// 个人信息页面逻辑
</script>

<style scoped>
.profile {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 30px;
}

.profile-header h1 {
  color: #2c3e50;
  margin-bottom: 8px;
}

.profile-header p {
  color: #7f8c8d;
  font-size: 1.1rem;
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
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 30px;
  align-items: start;
}

.avatar-section {
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: #ecf0f1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
}

.avatar .material-icons {
  font-size: 80px;
  color: #bdc3c7;
}

.edit-avatar-btn {
  background: none;
  border: 1px solid #3498db;
  color: #3498db;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.edit-avatar-btn:hover {
  background: #3498db;
  color: white;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
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
  color: #34495e;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-badge.verified {
  background: #d4edda;
  color: #155724;
}

.action-buttons {
  grid-column: 1 / -1;
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
}

.btn-primary {
  background: #3498db;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
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