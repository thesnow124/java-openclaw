<template>
  <div id="app">
    <el-container style="height: 100vh">
      <el-aside width="200px" style="background-color: #304156">
        <div class="logo">
          <h2>OpenClaw Lite</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>Dashboard</span>
          </el-menu-item>
          <el-menu-item index="/channels">
            <el-icon><ChatDotRound /></el-icon>
            <span>Channels</span>
          </el-menu-item>
          <el-menu-item index="/sessions">
            <el-icon><ChatLineSquare /></el-icon>
            <span>Sessions</span>
          </el-menu-item>
          <el-menu-item index="/agents">
            <el-icon><User /></el-icon>
            <span>Agents</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header>
          <div class="header-content">
            <span>OpenClaw Lite Management Console</span>
            <el-badge :value="status.channels?.running || 0" class="item">
              Channels Running
            </el-badge>
          </div>
        </el-header>
        
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useAdminStore} from './stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

const activeMenu = ref('/')
const status = ref({
  channels: { running: 0 },
  sessions: { total: 0 },
  agents: { total: 0 }
})

onMounted(() => {
  adminStore.fetchStatus()
  status.value = adminStore.status
})
</script>

<style scoped>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
}

.logo {
  padding: 20px;
  text-align: center;
}

.logo h2 {
  color: #fff;
  margin: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item {
  margin-left: 20px;
}
</style>
