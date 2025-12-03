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
import axios from 'axios'

const router = useRouter()
const searchKeyword = ref('')
const showSearchResults = ref(false)
const searchResults = ref<any[]>([])
const currentlyHighlighted = ref<number | null>(null)
const tools = ref<any[]>([])

let map: any = null
let myLocationMarker: any = null
const toolMarkers = new Map<number, any>()

const MY_POSITION = {
  lng: 116.238549,
  lat: 40.141686
}

// 前端测试数据（后端不可用时降级使用）
const MOCK_TOOLS = [
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

// 对同一地点的工具进行分组
const groupToolsByLocation = (toolList: any[]) => {
  const locationGroups = new Map<string, any[]>()
  
  toolList.forEach(tool => {
    // 使用经纬度作为唯一标识，四舍五入到小数点后5位
    const key = `${tool.lng.toFixed(5)},${tool.lat.toFixed(5)}`
    if (!locationGroups.has(key)) {
      locationGroups.set(key, [])
    }
    locationGroups.get(key)?.push(tool)
  })
  
  return Array.from(locationGroups.values())
}

// 添加工具标记（聚合显示）
const addToolMarkers = (toolList: any[]) => {
  // 清除旧标记
  toolMarkers.forEach(marker => marker.setMap(null))
  toolMarkers.clear()

  const locationGroups = groupToolsByLocation(toolList)
  
  locationGroups.forEach(toolGroup => {
    const firstTool = toolGroup[0]
    const availableCount = toolGroup.filter((t: any) => t.status === 'available').length
    const totalCount = toolGroup.length
    
    // 确定标记颜色
    let iconColor = '#52c41a' // 默认绿色（可用）
    if (availableCount === 0) {
      iconColor = '#faad14' // 所有工具都已借出时显示黄色
    } else if (availableCount < totalCount) {
      iconColor = '#ff7875' // 部分可用时显示红色
    }

    // 创建标记内容，显示工具数量
    const markerContent = `<div style="
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
    toolGroup.forEach((tool: any) => {
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

    // 为搜索功能存储每个工具的 marker
    toolGroup.forEach((tool: any) => {
      toolMarkers.set(tool.id, marker)
    })
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

// 获取工具列表
const fetchTools = async () => {
  try {
    // 尝试从后端获取数据
    const response = await axios.get('/api/published-tools/search')
    if (response.data && response.data.length > 0) {
      tools.value = response.data.map((tool: any) => ({
        id: tool.id,
        name: tool.toolName,
        lng: tool.longitude || MY_POSITION.lng,
        lat: tool.latitude || MY_POSITION.lat,
        location: tool.location,
        status: tool.status
      }))
    } else {
      // 后端返回空数据，使用测试数据
      tools.value = MOCK_TOOLS
    }
  } catch (error) {
    // 后端请求失败，使用测试数据
    console.warn('后端不可用，使用前端测试数据:', error)
    tools.value = MOCK_TOOLS
  }
}

const initMap = async () => {
  try {
    await AMapLoader.load({
      key: 'b89c154dcf2c17dcac9ca55afb3ed734',
      version: '2.0',
      plugins: ['AMap.Marker', 'AMap.InfoWindow', 'AMap.Scale'],
    })

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
    addToolMarkers(tools.value)

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

// 核心：搜索功能
const performSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    clearSearch()
    return
  }

  const matchedTools = tools.value.filter(tool =>
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

  // 临时保存原始标记内容
  const originalContents = new Map<string, string>()
  const uniqueMarkers = new Set<any>()
  matchedTools.forEach(tool => {
    const marker = toolMarkers.get(tool.id)
    if (marker) {
      uniqueMarkers.add(marker)
      const pos = marker.getPosition()
      const key = `${pos.lng.toFixed(5)},${pos.lat.toFixed(5)}`
      if (!originalContents.has(key)) {
        originalContents.set(key, marker.getContent())
      }
    }
  })

  // 重新渲染所有标记（恢复默认状态）
  addToolMarkers(tools.value)

  // 高亮匹配的标记
  uniqueMarkers.forEach(marker => {
    const pos = marker.getPosition()
    const key = `${pos.lng.toFixed(5)},${pos.lat.toFixed(5)}`
    
    // 找出该位置的所有工具
    const locationTools = tools.value.filter(t => 
      `${t.lng.toFixed(5)},${t.lat.toFixed(5)}` === key
    )
    
    // 找出匹配的工具
    const matchedInLocation = locationTools.filter(t => 
      matchedTools.some(m => m.id === t.id)
    )
    
    if (matchedInLocation.length > 0) {
      const availableMatched = matchedInLocation.filter(t => t.status === 'available').length
      const totalMatched = matchedInLocation.length
      let highlightColor = '#38761d' // 深绿色
      if (availableMatched === 0) {
        highlightColor = '#d48806' // 深黄色
      } else if (availableMatched < totalMatched) {
        highlightColor = '#cf1322' // 深红色
      }

      marker.setContent(`<div style="
        width: 30px;
        height: 30px;
        border-radius: 50%;
        background: ${highlightColor};
        border: 2px solid white;
        box-shadow: 0 0 0 4px rgba(24,144,255,0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-weight: bold;
        font-size: 12px;
      ">${locationTools.length}</div>`)
    }
  })

  // 聚焦到匹配区域
  const markerArray = Array.from(uniqueMarkers)
  if (markerArray.length > 0) {
    map.setFitView(markerArray, false, [80, 80, 80, 80])
  }

  ElMessage.success(`找到 ${matchedTools.length} 个匹配结果`)
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  showSearchResults.value = false
  searchResults.value = []
  currentlyHighlighted.value = null
  // 恢复默认标记
  addToolMarkers(tools.value)
}

// 🔹 修复：鼠标悬停高亮
const highlightResult = (toolId: number) => {
  currentlyHighlighted.value = toolId
  
  // 获取该工具的 marker
  const marker = toolMarkers.get(toolId)
  if (!marker) return
  
  // 创建高亮内容（深绿色）
  const highlightedContent = `<div style="
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: #38761d; /* 深绿色 */
    border: 2px solid white;
    box-shadow: 0 0 0 4px rgba(24,144,255,0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: bold;
    font-size: 12px;
  ">${getToolLocationCount(toolId)}</div>`
  
  marker.setContent(highlightedContent)
}

// 🔹 修复：鼠标移开恢复
const unhighlightResult = () => {
  currentlyHighlighted.value = null
  // 恢复所有标记为默认状态
  addToolMarkers(tools.value)
}

// 辅助函数：获取某个位置的工具总数
const getToolLocationCount = (toolId: number) => {
  const tool = tools.value.find(t => t.id === toolId)
  if (!tool) return 1
  
  // 统计同一位置的工具数量
  const locationKey = `${tool.lng.toFixed(5)},${tool.lat.toFixed(5)}`
  return tools.value.filter(t => 
    `${t.lng.toFixed(5)},${t.lat.toFixed(5)}` === locationKey
  ).length
}

// 点击结果定位
const zoomToResult = (result: any) => {
  map.setCenter([result.lng, result.lat])
  map.setZoom(18)
  showSearchResults.value = false
  currentlyHighlighted.value = result.id
}

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