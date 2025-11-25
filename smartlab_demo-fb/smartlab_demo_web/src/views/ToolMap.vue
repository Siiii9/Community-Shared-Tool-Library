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

import { onMounted, onUnmounted } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

let map: any = null
let myLocationMarker: any = null

// 🔹 我的位置（信息B座）—— 仅用于定位和蓝点，不是工具！
const MY_POSITION = {
  lng: 116.238549,
  lat: 40.141686
}

// 🔹 5个真实工具（分布在其他5栋楼）
const TOOLS = [
  { id: 1, name: '冲击钻', lng: 116.235718, lat: 40.141605, location: '工学A座-105工具间', status: 'available' },
  { id: 2, name: '万用表', lng: 116.238418, lat: 40.142330, location: '信息C座-301电子室', status: 'borrowed' },
  { id: 3, name: '电焊机', lng: 116.237475, lat: 40.141751, location: '信息A座-202车间', status: 'available' },
  { id: 4, name: '手电钻', lng: 116.236858, lat: 40.141954, location: '工学B座-101实验室', status: 'available' },
  { id: 5, name: '水平仪', lng: 116.238675, lat: 40.140950, location: '图书馆-工具角', status: 'borrowed' },
]

// 添加5个工具标记
const addToolMarkers = () => {
  TOOLS.forEach(tool => {
    const iconColor = tool.status === 'available' ? '#52c41a' : '#faad14'

    const marker = new window.AMap.Marker({
      position: [tool.lng, tool.lat],
      title: tool.name,
      map: map,
      content: `<div style="
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
        font-size: 14px;
      ">●</div>`,
      offset: new window.AMap.Pixel(-15, -15)
    })

    const infoWindow = new window.AMap.InfoWindow({
      content: `
        <div style="padding:12px; max-width:200px;">
          <h4 style="margin:0 0 8px 0;">${tool.name}</h4>
          <p><strong>位置：</strong>${tool.location}</p>
          <p><strong>状态：</strong>
            <span style="color: ${iconColor}">
              ${tool.status === 'available' ? '可用' : '已借出'}
            </span>
          </p>
          <div style="display:flex; gap:8px; margin-top:12px;">
            <button style="
              padding:4px 8px;
              background:#1890ff;
              color:white;
              border:none;
              border-radius:4px;
              cursor:pointer;
              font-size:12px;
            " onclick="window.toolDetailClick(${tool.id})">
              详细
            </button>
          </div>
        </div>
      `,
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

const refreshMap = () => {
  if (map) {
    map.clearMap()
    addMyLocationMarker()
    addToolMarkers()
    ElMessage.info('地图已刷新')
  }
}

const openFilter = () => {
  ElMessage.info('筛选功能开发中')
}

// 首次进入自动加载
onMounted(() => {
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