<template>
  <div class="todo-center-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <span>📋 待办审批</span>
          <el-button link type="primary" @click="loadList">刷新</el-button>
        </div>
      </template>

      <div v-loading="loading" class="todo-list">
        <div
          v-for="todo in todos"
          :key="todo.id"
          class="todo-item"
          @click="handleClick(todo)"
        >
          <div class="todo-title" :title="todo.title">{{ todo.title }}</div>
          <div class="todo-meta">
            <div class="todo-tags">
              <el-tag :type="getTagType(todo.module)" size="small">{{ todo.module }}</el-tag>
              <el-tag type="warning" size="small">{{ todo.status }}</el-tag>
            </div>
            <span class="todo-time">{{ todo.time }}</span>
          </div>
        </div>
        <el-empty v-if="!loading && todos.length === 0" description="暂无待办事项" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTodos } from '@/api/dashboard'

const router = useRouter()
const loading = ref(false)
const todos = ref([])

const getTagType = (module) => {
  const map = { '公物仓': 'primary', '用车': 'success', '公寓': 'warning', '食堂': 'danger' }
  return map[module] || 'info'
}

const handleClick = (todo) => {
  if (todo && todo.path) {
    router.push(todo.path)
    return
  }
  // 无路径时按模块回退到对应功能页
  const modulePathMap = {
    '公物仓': '/gc/borrow-apply',
    '用车': '/cl/apply',
    '公寓': '/gy/occupant',
    '食堂': '/st/purchase'
  }
  router.push((todo && modulePathMap[todo.module]) || '/')
}

const loadList = async () => {
  loading.value = true
  try {
    todos.value = (await getTodos()) || []
  } catch (e) {
    todos.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
}

.todo-list {
  min-height: 200px;
}

.todo-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.15s;
}

.todo-item:hover {
  background: #f5f7fa;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.todo-tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.todo-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}
</style>