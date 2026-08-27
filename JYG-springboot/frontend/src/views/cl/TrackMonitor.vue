<template>
  <div class="track-page">
    <!-- 顶部控制栏 -->
    <div class="toolbar">
      <span class="toolbar-title">车辆运行监控</span>
      <div class="toolbar-actions">
        <el-button type="primary" :icon="Refresh" :loading="loading" @click="loadVehicles">刷新</el-button>
        <el-button
          type="warning"
          :disabled="!selectedVehicle"
          @click="openReplay"
        >
          轨迹回放
        </el-button>
        <el-button
          :disabled="!selectedVehicle"
          @click="handleSimulate"
        >
          模拟轨迹（测试）
        </el-button>
      </div>
    </div>

    <div class="body">
      <!-- 左侧车辆列表 -->
      <div class="side-panel">
        <div class="panel-title">出车中车辆（{{ vehicles.length }}）</div>
        <div v-loading="loading" class="vehicle-list">
          <div
            v-for="v in vehicles"
            :key="v.vehicleId"
            class="vehicle-item"
            :class="{ active: selectedVehicle && selectedVehicle.vehicleId === v.vehicleId }"
            @click="selectVehicle(v)"
          >
            <span class="status-dot" :style="{ backgroundColor: statusColor(v.status) }" />
            <div class="vehicle-info">
              <div class="vehicle-plate">
                {{ v.plateNumber }}
                <el-tag size="small" :type="statusTagType(v.status)">{{ v.statusLabel || v.status }}</el-tag>
              </div>
              <div class="vehicle-meta">驾驶员：{{ v.driverName || '-' }}</div>
              <div class="vehicle-meta">
                位置：{{ v.lng ? `${v.lng.toFixed(4)}, ${v.lat.toFixed(4)}` : '暂无轨迹' }}
              </div>
              <div class="vehicle-meta">
                速度：<span class="speed">{{ v.speed ? v.speed + ' km/h' : '-' }}</span>
                <span v-if="v.destination" class="destination">目的地：{{ v.destination }}</span>
              </div>
            </div>
          </div>
          <el-empty v-if="!loading && !vehicles.length" description="暂无出车中车辆" :image-size="60" />
        </div>
      </div>

      <!-- 中间地图区域 -->
      <div class="map-wrap">
        <div ref="mapRef" class="map-container" />
        <div v-if="!mapReady" class="map-tip">
          <el-empty v-if="!mapKey" description="请先在 src/config/amap.config.js 中配置高德地图 Key 与安全密钥" :image-size="90">
            <el-button type="primary" @click="initMap">我已配置，重新加载</el-button>
          </el-empty>
          <el-empty v-else description="地图加载中..." :image-size="90" />
        </div>
      </div>
    </div>

    <!-- 轨迹回放对话框 -->
    <el-dialog v-model="replayVisible" title="轨迹回放" width="560px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="选择车辆">
          <el-select v-model="replayForm.vehicleId" placeholder="请选择车辆" style="width: 100%">
            <el-option
              v-for="v in vehicles"
              :key="v.vehicleId"
              :label="v.plateNumber"
              :value="v.vehicleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="replayForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeReplay">关闭</el-button>
        <el-button type="primary" :loading="querying" @click="queryReplay">查询轨迹</el-button>
        <el-button type="success" :disabled="!replayPath.length" @click="startPlayback">
          {{ playing ? '播放中...' : '播放轨迹' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import amapConfig from '@/config/amap.config'
import { getAllCurrentLocations, getTrackHistory, simulateTrack } from '@/api/cl'

/** 状态颜色与标签 */
const statusColorMap = {
  ONGOING: '#409eff',
  WAITING: '#e6a23c',
  RETURNED: '#67c23a',
  CANCELLED: '#909399'
}
const statusTagMap = {
  ONGOING: 'primary',
  WAITING: 'warning',
  RETURNED: 'success',
  CANCELLED: 'info'
}
const statusColor = (s) => statusColorMap[s] || '#409eff'
const statusTagType = (s) => statusTagMap[s] || 'primary'

const mapKey = amapConfig.key && amapConfig.securityJsCode

/** 地图状态 */
const mapRef = ref()
const map = ref(null)
const amap = ref(null)
const mapReady = ref(false)
const markers = new Map() // vehicleId -> marker
const infoWindow = ref(null)
const polyline = ref(null)
const moveMarker = ref(null)
let moveTimer = null

/** 车辆数据 */
const loading = ref(false)
const vehicles = ref([])
const selectedVehicle = ref(null)

/** 初始化地图 */
const initMap = async () => {
  if (!mapKey) return
  mapReady.value = true
  try {
    const AMap = await AMapLoader.load({
      key: amapConfig.key,
      securityJsCode: amapConfig.securityJsCode,
      version: '2.0',
      plugins: ['AMap.Scale', 'AMap.ToolBar']
    })
    amap.value = AMap
    map.value = new AMap.Map(mapRef.value, {
      zoom: 12,
      center: [98.2891, 39.7732], // 嘉峪关市政府
      viewMode: '2D'
    })
    map.value.addControl(new AMap.Scale())
    map.value.addControl(new AMap.ToolBar())
    infoWindow.value = new AMap.InfoWindow({ offset: new AMap.Pixel(0, -30) })
    renderMarkers()
  } catch (e) {
    mapReady.value = false
    ElMessage.error('地图加载失败，请检查高德 Key 配置')
  }
}

/** 加载出车中车辆实时位置 */
const loadVehicles = async () => {
  loading.value = true
  try {
    vehicles.value = (await getAllCurrentLocations()) || []
    if (selectedVehicle.value) {
      const still = vehicles.value.find((v) => v.vehicleId === selectedVehicle.value.vehicleId)
      selectedVehicle.value = still || null
    }
    renderMarkers()
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    loading.value = false
  }
}

/** 渲染/更新车辆标记 */
const renderMarkers = () => {
  if (!amap.value || !map.value) return
  // 移除已下线的车辆标记
  const liveIds = new Set(vehicles.value.map((v) => v.vehicleId))
  markers.forEach((marker, id) => {
    if (!liveIds.has(id)) {
      marker.setMap(null)
      markers.delete(id)
    }
  })
  vehicles.value.forEach((v) => {
    if (v.lng == null || v.lat == null) return
    const pos = [v.lng, v.lat]
    let marker = markers.get(v.vehicleId)
    if (marker) {
      marker.setPosition(pos)
      marker.setContent(buildMarkerContent(v))
    } else {
      marker = new amap.value.Marker({
        position: pos,
        content: buildMarkerContent(v),
        offset: new amap.value.Pixel(-16, -16)
      })
      marker.on('click', () => showInfoWindow(v))
      markers.set(v.vehicleId, marker)
      marker.setMap(map.value)
    }
  })
}

/** 生成标记内容（圆形状态色标） */
const buildMarkerContent = (v) => {
  return `<div class="amap-vehicle-marker" style="background:${statusColor(v.status)}">
    ${v.plateNumber ? v.plateNumber.slice(-2) : ''}</div>`
}

/** 显示车辆信息窗口 */
const showInfoWindow = (v) => {
  if (!amap.value || !infoWindow.value || v.lng == null) return
  infoWindow.value.setContent(`
    <div class="amap-vehicle-info">
      <div><b>车牌号</b>：${v.plateNumber || '-'}</div>
      <div><b>驾驶员</b>：${v.driverName || '-'}</div>
      <div><b>速度</b>：${v.speed ? v.speed + ' km/h' : '-'}</div>
      <div><b>目的地</b>：${v.destination || '-'}</div>
      <div><b>上报时间</b>：${formatTime(v.lastUpdateTime)}</div>
    </div>`)
  infoWindow.value.open(map.value, [v.lng, v.lat])
}

/** 点击列表车辆：地图定位 + 信息窗口 */
const selectVehicle = (v) => {
  selectedVehicle.value = v
  if (map.value && v.lng != null) {
    map.value.setCenter([v.lng, v.lat])
    map.value.setZoom(13)
    showInfoWindow(v)
  }
}

/** 手动触发模拟轨迹 */
const handleSimulate = async () => {
  if (!selectedVehicle.value) return
  try {
    await simulateTrack(selectedVehicle.value.dispatchId)
    ElMessage.success('模拟轨迹已生成，点击刷新查看最新位置')
    loadVehicles()
  } catch (e) {
    // 错误已由拦截器统一提示
  }
}

/** 轨迹回放 */
const replayVisible = ref(false)
const querying = ref(false)
const playing = ref(false)
const replayPath = ref([])
const replayForm = reactive({
  vehicleId: null,
  timeRange: []
})

const openReplay = () => {
  if (!selectedVehicle.value) return
  replayForm.vehicleId = selectedVehicle.value.vehicleId
  const now = new Date()
  const start = new Date(now.getTime() - 60 * 60 * 1000)
  replayForm.timeRange = [formatDateTime(start), formatDateTime(now)]
  replayPath.value = []
  playing.value = false
  replayVisible.value = true
}

const queryReplay = async () => {
  if (!replayForm.vehicleId) {
    ElMessage.warning('请选择车辆')
    return
  }
  if (!replayForm.timeRange || replayForm.timeRange.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }
  querying.value = true
  try {
    const vo = await getTrackHistory({
      vehicleId: replayForm.vehicleId,
      startTime: replayForm.timeRange[0],
      endTime: replayForm.timeRange[1]
    })
    const pts = (vo.points || []).map((p) => [p.lng, p.lat])
    replayPath.value = pts
    drawPolyline(pts)
    if (pts.length) {
      map.value.setFitView([polyline.value])
    } else {
      ElMessage.warning('该时间段暂无轨迹数据')
    }
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    querying.value = false
  }
}

/** 绘制轨迹线 */
const drawPolyline = (pts) => {
  if (!amap.value || !map.value) return
  if (polyline.value) {
    polyline.value.setMap(null)
  }
  polyline.value = new amap.value.Polyline({
    path: pts,
    strokeColor: '#409eff',
    strokeWeight: 4,
    strokeOpacity: 0.8,
    lineJoin: 'round'
  })
  polyline.value.setMap(map.value)
}

/** 播放轨迹动画（车辆沿路径移动） */
const startPlayback = () => {
  if (!amap.value || !map.value || !replayPath.value.length || playing.value) return
  stopMoveMarker()
  const path = replayPath.value
  moveMarker.value = new amap.value.Marker({
    position: path[0],
    offset: new amap.value.Pixel(-16, -16),
    content: '<div class="amap-vehicle-marker amap-moving" style="background:#f56c6c">车</div>'
  })
  moveMarker.value.setMap(map.value)
  playing.value = true
  let i = 0
  moveTimer = setInterval(() => {
    i += 1
    if (i >= path.length) {
      stopMoveMarker()
      return
    }
    moveMarker.value.setPosition(path[i])
    map.value.setCenter(path[i])
  }, 500)
}

const stopMoveMarker = () => {
  if (moveTimer) {
    clearInterval(moveTimer)
    moveTimer = null
  }
  if (moveMarker.value) {
    moveMarker.value.setMap(null)
    moveMarker.value = null
  }
  playing.value = false
}

/** 关闭回放对话框并停止动画 */
const closeReplay = () => {
  stopMoveMarker()
  replayVisible.value = false
}

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const pad = (n) => String(n).padStart(2, '0')
const formatDateTime = (d) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`

onMounted(() => {
  initMap()
  loadVehicles()
})

onBeforeUnmount(() => {
  stopMoveMarker()
  if (polyline.value) polyline.value.setMap(null)
  if (map.value) map.value.destroy()
})
</script>

<style scoped>
.track-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 12px;
}

.toolbar-title {
  font-size: 15px;
  font-weight: 600;
}

.body {
  display: flex;
  flex: 1;
  min-height: 0;
}

.side-panel {
  width: 300px;
  margin-right: 12px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-title {
  padding: 10px 12px;
  font-weight: 600;
  border-bottom: 1px solid #e4e7ed;
}

.vehicle-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.vehicle-item {
  display: flex;
  align-items: flex-start;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.vehicle-item:hover {
  border-color: #409eff;
}

.vehicle-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 6px;
  margin-right: 8px;
  flex-shrink: 0;
}

.vehicle-info {
  flex: 1;
  min-width: 0;
}

.vehicle-plate {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.vehicle-meta {
  font-size: 12px;
  color: #909399;
  line-height: 1.7;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.speed {
  color: #e6a23c;
  font-weight: 600;
}

.destination {
  margin-left: 8px;
}

.map-wrap {
  flex: 1;
  position: relative;
  min-width: 0;
  border-radius: 4px;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 100%;
}

.map-tip {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
</style>

<style>
/* 地图标记样式（非 scoped，作用于动态创建的 DOM） */
.amap-vehicle-marker {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  cursor: pointer;
}

.amap-vehicle-marker.amap-moving {
  z-index: 999;
}

.amap-vehicle-info {
  font-size: 13px;
  line-height: 1.8;
  min-width: 160px;
}
</style>
