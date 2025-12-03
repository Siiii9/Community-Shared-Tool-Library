<!-- src/views/ToolMap.vue -->
<template>
  <div class="tool-map">
    <!-- 搜索框 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索工具名称或位置（如：冲击钻、文理楼）"
        clearable
        @keyup.enter="performSearch"
        @clear="clearSearch"
        class="search-input"
      >
        <!-- 右侧：搜索按钮 -->
        <template #append>
          <el-button @click="performSearch" type="primary" :icon="Search" />
        </template>
      </el-input>
    </div>

    <!-- 搜索结果面板 -->
    <div
      v-if="showSearchResults && searchResults.length > 0"
      class="search-results-panel"
    >
      <div
        v-for="(result, index) in searchResults"
        :key="result.id"
        class="result-item"
        @click="zoomToResult(result)"
        @mouseenter="highlightResult(result.id)"
        @mouseleave="unhighlightResult"
      >
        <div class="result-info">
          <span class="result-index">{{ index + 1 }}.</span>
          <span class="result-name">{{ result.name }}</span>
          <span class="result-location">({{ result.location }})</span>
        </div>
        <div class="result-status" :class="result.status">
          {{ result.status === 'available' ? '可用' : '已借出' }}
        </div>
      </div>
    </div>

    <div class="map-container">
      <div id="amap-container" class="amap-real-map"></div>
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

import { ref, onMounted, onUnmounted } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const searchKeyword = ref('')
const showSearchResults = ref(false)
const searchResults = ref<any[]>([])
const currentlyHighlighted = ref<number | null>(null)

let map: any = null
let myLocationMarker: any = null
const toolMarkers = new Map<number, any>()

const MY_POSITION = {
  lng: 116.238549,
  lat: 40.141686
}

const TOOLS = [
  { id: 1, name: '冲击钻', lng: 116.235718, lat: 40.141605, location: '工学A座-105工具间', status: 'available' },
  { id: 2, name: '万用表', lng: 116.238418, lat: 40.142330, location: '信息C座-301电子室', status: 'borrowed' },
  { id: 3, name: '电焊机', lng: 116.237475, lat: 40.141751, location: '信息A座-202车间', status: 'available' },
  { id: 4, name: '手电钻', lng: 116.236858, lat: 40.141954, location: '工学B座-101实验室', status: 'available' },
  { id: 5, name: '水平仪', lng: 116.238675, lat: 40.140950, location: '图书馆-工具角', status: 'borrowed' },
  { id: 6, name: '冲击钻', lng: 116.235878, lat: 40.141123, location: '文理楼B', status: 'available' },
  { id: 7, name: '万用表', lng: 116.235368, lat: 40.140513, location: '文理楼C', status: 'available' },
  { id: 8, name: '水平仪', lng: 116.236813, lat: 40.140505, location: '文理楼A', status: 'available' },
  { id: 9, name: '万用表', lng: 116.240475, lat: 40.140618, location: '学生发展中心', status: 'available' },
  { id: 10, name: '万用表', lng: 116.239474, lat: 40.142992, location: '瑞幸咖啡店', status: 'available' },
]

const initToolMarkers = () => {
  TOOLS.forEach(tool => {
    const iconColor = tool.status === 'available' ? '#52c41a' : '#faad14'

    const marker = new window.AMap.Marker({
      position: [tool.lng, tool.lat],
      title: `${tool.name} - ${tool.location}`,
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

    toolMarkers.set(tool.id, marker)
  })
}

const updateToolMarkers = (highlightIds: number[] = []) => {
  toolMarkers.forEach((marker, id) => {
    const tool = TOOLS.find(t => t.id === id)
    if (!tool) return

    const isHighlighted = highlightIds.includes(id)
    const iconColor = tool.status === 'available' ? (isHighlighted ? '#38761d' : '#52c41a') : '#faad14'

    marker.setContent(`<div style="
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
    ">●</div>`)
  })
}

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

const initMap = async () => {
  try {
    await AMapLoader.load({
      key: 'b89c154dcf2c17dcac9ca55afb3ed734',
      version: '2.0',
      plugins: ['AMap.Marker', 'AMap.InfoWindow', 'AMap.Scale'],
    })

    // 🔥 关键：初始化时确保交互开启
    map = new window.AMap.Map('amap-container', {
      zoom: 17,
      center: [MY_POSITION.lng, MY_POSITION.lat],
      viewMode: '3D',
      dragEnable: true,    // 允许拖拽
      zoomEnable: true,    // 允许缩放
      keyboardEnable: false,
    })

    map.addControl(new window.AMap.Scale({
      position: { bottom: '10px', left: '10px' }
    }))

    addMyLocationMarker()
    initToolMarkers()

    window.toolDetailClick = (toolId: number) => {
      router.push({ name: 'ToolDetail', params: { id: toolId } })
    }

    ElMessage.success('地图加载完成')

    setTimeout(() => {
      createCustomControls()
    }, 500)

  } catch (error: any) {
    console.error('地图加载失败:', error)
    ElMessage.error('地图初始化失败')
  }
}

const createCustomControls = () => {
  const controlContainer = document.createElement('div')
  controlContainer.style.position = 'absolute'
  controlContainer.style.bottom = '10px'
  controlContainer.style.right = '10px'
  controlContainer.style.display = 'flex'
  controlContainer.style.flexDirection = 'column'
  controlContainer.style.gap = '8px'
  controlContainer.style.zIndex = '1000'

  const locateBtn = document.createElement('div')
  locateBtn.innerHTML = `
    <div style="
      width: 40px;
      height: 40px;
      background: white;
      border: 1px solid #ccc;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #1890ff;
      font-size: 20px;
      cursor: pointer;
      transition: background 0.2s, box-shadow 0.2s;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    " title="定位到我的位置">
      <span class="material-icons">my_location</span>
    </div>
  `
  locateBtn.onclick = () => {
    map.setCenter([MY_POSITION.lng, MY_POSITION.lat])
    map.setZoom(17)
    ElMessage.info('已定位到当前位置')
  }

  const zoomInBtn = document.createElement('div')
  zoomInBtn.innerHTML = `
    <div style="
      width: 40px;
      height: 40px;
      background: white;
      border: 1px solid #ccc;
      border-top: none;
      border-radius: 0 0 4px 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #333;
      font-size: 24px;
      cursor: pointer;
      transition: background 0.2s, box-shadow 0.2s;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    " title="放大地图">
      +
    </div>
  `
  zoomInBtn.onclick = () => map.zoomIn()

  const zoomOutBtn = document.createElement('div')
  zoomOutBtn.innerHTML = `
    <div style="
      width: 40px;
      height: 40px;
      background: white;
      border: 1px solid #ccc;
      border-top: none;
      border-radius: 0 0 4px 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #333;
      font-size: 24px;
      cursor: pointer;
      transition: background 0.2s, box-shadow 0.2s;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    " title="缩小地图">
      -
    </div>
  `
  zoomOutBtn.onclick = () => map.zoomOut()

  controlContainer.appendChild(locateBtn)
  controlContainer.appendChild(zoomInBtn)
  controlContainer.appendChild(zoomOutBtn)

  const amapContainer = document.getElementById('amap-container')
  if (amapContainer) {
    amapContainer.appendChild(controlContainer)
  }
}

// 🔥 核心修复：使用 setFitView 替代 setBounds
const performSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    clearSearch()
    return
  }

  const matchedTools = TOOLS.filter(tool =>
    tool.name.toLowerCase().includes(keyword.toLowerCase()) ||
    tool.location.toLowerCase().includes(keyword.toLowerCase())
  )

  if (matchedTools.length === 0) {
    ElMessage.warning('未找到匹配的工具')
    clearSearch()
    return
  }

  searchResults.value = matchedTools
  showSearchResults.value = true

  const matchedIds = matchedTools.map(t => t.id)
  updateToolMarkers(matchedIds)

  // 🔥 关键：获取匹配的 marker 实例
  const matchedMarkers = matchedIds
    .map(id => toolMarkers.get(id))
    .filter(marker => marker != null)

  if (matchedMarkers.length > 0) {
    // 使用 setFitView 自动调整视野，且不会锁定交互！
    map.setFitView(matchedMarkers, false, [80, 80, 80, 80])
  }

  ElMessage.success(`找到 ${matchedTools.length} 个匹配结果`)
}

const clearSearch = () => {
  searchKeyword.value = ''
  showSearchResults.value = false
  searchResults.value = []
  currentlyHighlighted.value = null
  // 恢复所有点为默认状态
  updateToolMarkers([])
}

const highlightResult = (toolId: number) => {
  currentlyHighlighted.value = toolId
  updateToolMarkers([toolId])
}

const unhighlightResult = () => {
  currentlyHighlighted.value = null
  if (showSearchResults.value && searchResults.value.length > 0) {
    const matchedIds = searchResults.value.map(t => t.id)
    updateToolMarkers(matchedIds)
  } else {
    updateToolMarkers([])
  }
}

const zoomToResult = (result: any) => {
  map.setCenter([result.lng, result.lat])
  map.setZoom(18)
  showSearchResults.value = false
  currentlyHighlighted.value = result.id
  setTimeout(() => {
    updateToolMarkers([result.id])
  }, 100)
}

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

.search-bar {
  display: flex;
  gap: 12px;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.search-input {
  width: 100%;
  max-width: 600px;
}

:deep(.el-input-group__append .el-button) {
  height: 100%;
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

.search-results-panel {
  position: absolute;
  top: 60px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 500px;
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
  padding: 12px;
}

.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}

.result-item:hover {
  background: #f5f7fa;
}

.result-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.result-index {
  font-weight: bold;
  color: #999;
}

.result-name {
  font-weight: bold;
  color: #333;
}

.result-location {
  color: #777;
  font-size: 12px;
}

.result-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.result-status.available {
  background: #52c41a;
}

.result-status.borrowed {
  background: #faad14;
}

.map-container {
  flex: 1;
  min-height: 0;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  position: relative;
}

#amap-container {
  width: 100%;
  height: 100%;
}
</style>