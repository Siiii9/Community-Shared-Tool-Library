<!-- src/views/ToolMap.vue -->
<template>
  <div class="tool-map">
    <div class="map-header">
      <p>浏览附近可用的共享工具</p>
    </div>

    <div class="map-container">
      <div id="amap-container" class="amap-real-map"></div>
    </div>

    <div class="quick-actions">
      <button class="action-btn" @click="locateAndReload">
        <span class="material-icons">my_location</span>
        定位到我的位置
      </button>
      <button class="action-btn" @click="openFilter">
        <span class="material-icons">tune</span>
        筛选工具
      </button>
      <button class="action-btn" @click="refreshMap">
        <span class="material-icons">refresh</span>
        刷新地图
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
declare global {
  interface Window {
    AMap: any;
    toolDetailClick: (toolId: number) => void;
  }
}

import { onMounted, onUnmounted, ref } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

let map: any = null
let myLocationMarker: any = null

// 🔹 我的位置（信息B座）—— 仅用于定位和蓝点，不是工具！
const MY_POSITION = {
  lng: 116.238549,
  lat: 40.141686
}

// 工具列表
const tools = ref([])

// 对同一地点的工具进行分组
const groupToolsByLocation = () => {
  const locationGroups = new Map<string, any[]>()
  
  tools.value.forEach(tool => {
    // 使用经纬度作为唯一标识，考虑到浮点数精度问题，四舍五入到小数点后5位
    const key = `${tool.lng.toFixed(5)},${tool.lat.toFixed(5)}`
    if (!locationGroups.has(key)) {
      locationGroups.set(key, [])
    }
    locationGroups.get(key)?.push(tool)
  })
  
  return Array.from(locationGroups.values())
}

// 添加工具标记
const addToolMarkers = () => {
  const locationGroups = groupToolsByLocation()
  
  locationGroups.forEach(toolGroup => {
    const firstTool = toolGroup[0]
    const availableCount = toolGroup.filter(tool => tool.status === 'available').length
    const totalCount = toolGroup.length
    
    // 确定标记颜色
    let iconColor = '#52c41a' // 默认绿色（可用）
    if (availableCount === 0) {
      iconColor = '#faad14' // 所有工具都已借出时显示黄色
    } else if (availableCount < totalCount) {
      iconColor = '#ff7875' // 部分可用时显示红色
    }

    // 创建标记内容，显示工具数量
    let markerContent = `<div style="
      width: 30px;
      height: 30px;
      border-radius: 50%;
      background: ${iconColor};
      border: 2px solid white;
      box-shadow: 0 2px 6px rgba(0,0,0,0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: bold;
      font-size: 12px;
    ">${totalCount}</div>`

    const marker = new window.AMap.Marker({
      position: [firstTool.lng, firstTool.lat],
      title: `${firstTool.location} (${totalCount}个工具)`,
      map: map,
      content: markerContent,
      offset: new window.AMap.Pixel(-15, -15)
    })

    // 创建信息窗口内容，显示该地点的所有工具列表
    let infoWindowContent = `
      <div style="padding:12px; max-width:250px;">
        <h4 style="margin:0 0 10px 0;">${firstTool.location}</h4>
        <p style="margin:0 0 10px 0; color:#666;">共${totalCount}个工具，${availableCount}个可用</p>
        <div style="max-height:200px; overflow-y:auto;">
    `
    
    // 添加工具列表
    toolGroup.forEach(tool => {
      const toolStatus = tool.status === 'available' ? '可用' : '已借出'
      const statusColor = tool.status === 'available' ? '#52c41a' : '#faad14'
      
      infoWindowContent += `
        <div style="
          padding:8px;
          margin-bottom:8px;
          border-radius:4px;
          background:#f5f5f5;
          cursor:pointer;
          transition:background 0.2s;
        " onmouseover="this.style.background='#e8f4f8'" onmouseout="this.style.background='#f5f5f5'" onclick="window.toolDetailClick(${tool.id})">
          <div style="display:flex; justify-content:space-between; align-items:center;">
            <span style="font-weight:500;">${tool.name}</span>
            <span style="color:${statusColor}; font-size:12px;">${toolStatus}</span>
          </div>
        </div>
      `
    })
    
    infoWindowContent += `
        </div>
        <div style="margin-top:10px; font-size:12px; color:#999;">
          点击工具名称查看详情
        </div>
      </div>
    `

    const infoWindow = new window.AMap.InfoWindow({
      content: infoWindowContent,
      offset: new window.AMap.Pixel(0, -10)
    })

    marker.on('click', () => {
      infoWindow.open(map, marker.getPosition())
    })
  })
}

// 添加“我的位置”蓝点（高德风格）
const addMyLocationMarker = () => {
  if (myLocationMarker) map.remove(myLocationMarker)
  myLocationMarker = new window.AMap.Marker({
    position: [MY_POSITION.lng, MY_POSITION.lat],
    map: map,
    content: `<div style="
      width: 24px;
      height: 24px;
      border-radius: 50%;
      background: #1890ff;
      border: 2px solid white;
      box-shadow: 0 0 0 4px rgba(24, 144, 255, 0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 12px;
    ">📍</div>`,
    offset: new window.AMap.Pixel(-12, -12),
    title: '我的位置',
    clickable: false
  })
}

// 初始化地图
const initMap = async () => {
  try {
    await AMapLoader.load({
      key: 'b89c154dcf2c17dcac9ca55afb3ed734',
      version: '2.0',
      plugins: ['AMap.Marker', 'AMap.InfoWindow', 'AMap.ToolBar', 'AMap.Scale'],
    })

    // 地图中心 = 我的位置（信息B座）
    map = new window.AMap.Map('amap-container', {
      zoom: 17,
      center: [MY_POSITION.lng, MY_POSITION.lat],
      viewMode: '3D',
    })

    map.addControl(new window.AMap.ToolBar())
    map.addControl(new window.AMap.Scale())

    // 添加我的位置蓝点
    addMyLocationMarker()

    // 添加5个工具点
    addToolMarkers()

    // 注册跳转
    window.toolDetailClick = (toolId: number) => {
      router.push({ name: 'ToolDetail', params: { id: toolId } })
    }

    ElMessage.success('地图加载完成')
  } catch (error: any) {
    console.error('地图加载失败:', error)
    ElMessage.error('地图初始化失败')
  }
}

const locateAndReload = () => {
  if (map) {
    map.destroy()
    map = null
  }
  initMap()
}

const refreshMap = async () => {
  if (map) {
    await fetchTools()
    map.clearMap()
    addMyLocationMarker()
    addToolMarkers()
    ElMessage.info('地图已刷新')
  }
}

const openFilter = () => {
  ElMessage.info('筛选功能开发中')
}

// 获取工具列表
const fetchTools = async () => {
  try {
    // 使用search接口获取所有工具，不传递任何筛选参数
    const response = await axios.get('/api/published-tools/search')
    if (response.data && response.data.length > 0) {
      tools.value = response.data.map((tool: any) => ({
        id: tool.id,
        name: tool.toolName,
        lng: tool.longitude || 116.238549,
        lat: tool.latitude || 40.141686,
        location: tool.location,
        status: tool.status
      }))
    }
  } catch (error) {
    console.error('获取工具列表失败:', error)
    ElMessage.warning('获取工具列表失败，请检查网络连接')
  }
}

// 首次进入自动加载
onMounted(async () => {
  await fetchTools()
  initMap()
})

onUnmounted(() => {
  if (map) map.destroy()
})
</script>

<style scoped>
.tool-map {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.map-header {
  text-align: center;
  margin-bottom: 12px;
}

.map-header p {
  color: #7f8c8d;
  font-size: 1.1rem;
  margin: 0;
  font-weight: 500;
}

.map-container {
  flex: 1;
  min-height: 0;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

#amap-container {
  width: 100%;
  height: 100%;
}

.quick-actions {
  display: flex;
  gap: 14px;
  justify-content: center;
  margin-top: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
  font-weight: 500;
}

.action-btn:hover {
  background: #2980b9;
}

.action-btn .material-icons {
  font-size: 1.25rem;
}
</style>