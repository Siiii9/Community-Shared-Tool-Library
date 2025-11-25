<!-- src/views/ToolDetail.vue -->
<template>
  <div class="tool-detail">
    <div class="detail-header">
      <h1>{{ tool?.name }}</h1>
      <div class="status-badge" :class="tool?.status">
        {{ tool?.status === 'available' ? '可用' : '已借出' }}
      </div>
    </div>

    <div class="detail-content">
      <!-- 图片展示（模拟） -->
      <div class="image-section">
        <img :src="toolImage" alt="工具图片" class="main-image" />
      </div>

      <!-- 工具介绍 -->
      <div class="description">
        <h3>工具介绍</h3>
        <p>{{ tool?.description || '暂无介绍' }}</p>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button class="chat-btn" @click="goToChat">
          <span class="material-icons">chat</span>
          聊一聊
        </button>
        <button 
          class="borrow-btn" 
          @click="borrowTool" 
          :disabled="tool?.status !== 'available'"
        >
          <span class="material-icons">shopping_cart</span>
          借用
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const tool = ref<any>(null)

// 模拟工具数据（根据 ID 加载）
const loadToolById = (id: string) => {
  const tools = [
    { 
      id: '1', 
      name: '梯子', 
      status: 'available', 
      location: '信息B座-201实验室', 
      description: '铝合金伸缩梯，最大承重150kg，适合高处作业。附带防滑脚垫，安全可靠。' 
    },
    { 
      id: '2', 
      name: '冲击钻', 
      status: 'borrowed', 
      location: '信息B座-205电子间', 
      description: '博世专业级冲击钻，功率850W，带多种钻头，适用于混凝土和砖墙钻孔。' 
    },
    { 
      id: '3', 
      name: '万用表', 
      status: 'available', 
      location: '信息A座-101测试室', 
      description: '数字万用表，可测电压、电流、电阻、通断，精度高，带背光屏，操作简单。' 
    },
    { 
      id: '4', 
      name: '电焊机', 
      status: 'available', 
      location: '工程楼-301车间', 
      description: '小型便携式电焊机，适合薄板焊接，操作简单，安全性高，附带焊接面罩。' 
    },
    { 
      id: '5', 
      name: '手电钻', 
      status: 'borrowed', 
      location: '信息C座-105工具间', 
      description: '轻便手电钻，双速调节，适合家庭装修和小件加工，电池续航4小时。' 
    },
    { 
      id: '6', 
      name: '水平仪', 
      status: 'available', 
      location: '土木楼-401测量室', 
      description: '高精度激光水平仪，自动找平，红绿双线，适用于墙面、地面找平，误差±1mm。' 
    }
  ]

  const found = tools.find(t => t.id === id)
  tool.value = found || null
}

// 模拟图片（实际项目中从后端获取）
const toolImage = ref('/images/tool-placeholder.jpg') // 你可以替换为真实图片路径

onMounted(() => {
  loadToolById(route.params.id as string)
})

// 跳转到社区聊天页
const goToChat = () => {
  router.push('/community-chat')
}

// 借用工具
const borrowTool = () => {
  if (tool.value?.status !== 'available') return
  alert(`✅ 已成功提交借用申请：${tool.value.name}`)
  tool.value.status = 'borrowed'
}
</script>

<style scoped>
.tool-detail {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.detail-header h1 {
  color: #2c3e50;
  margin: 0;
  font-size: 1.8rem;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  color: white;
}

.status-badge.available {
  background: #52c41a;
}

.status-badge.borrowed {
  background: #faad14;
}

.image-section {
  text-align: center;
  margin-bottom: 20px;
}

.main-image {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.description {
  margin-bottom: 25px;
}

.description h3 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.description p {
  line-height: 1.7;
  color: #333;
  font-size: 1.05rem;
}

.action-buttons {
  display: flex;
  gap: 20px;
  justify-content: space-between;
}

.chat-btn, .borrow-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.chat-btn {
  background: #607D8B;
  color: white;
  flex: 1;
}

.chat-btn:hover {
  background: #455A64;
}

.borrow-btn {
  background: #4CAF50;
  color: white;
  flex: 1;
}

.borrow-btn:hover:not(:disabled) {
  background: #388E3C;
}

.borrow-btn:disabled {
  background: #bdbdbd;
  cursor: not-allowed;
}

.material-icons {
  font-size: 18px;
}
</style>