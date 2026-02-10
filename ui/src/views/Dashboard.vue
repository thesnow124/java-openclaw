<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat">
            <div class="stat-label">Channels</div>
            <div class="stat-value">{{ status.channels?.running || 0 }} / {{ status.channels?.total || 0 }}</div>
            <div class="stat-desc">Running</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat">
            <div class="stat-label">Sessions</div>
            <div class="stat-value">{{ status.sessions?.total || 0 }}</div>
            <div class="stat-desc">Total</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat">
            <div class="stat-label">Agents</div>
            <div class="stat-value">{{ status.agents?.total || 0 }}</div>
            <div class="stat-desc">Configured</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat">
            <div class="stat-label">Status</div>
            <div class="stat-value success">{{ status.status }}</div>
            <div class="stat-desc">System</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>Quick Actions</span>
          </template>
          <el-row :gutter="10">
            <el-col :span="4">
              <el-button type="primary" @click="$router.push('/channels')">
                Manage Channels
              </el-button>
            </el-col>
            <el-col :span="4">
              <el-button type="success" @click="$router.push('/sessions')">
                View Sessions
              </el-button>
            </el-col>
            <el-col :span="4">
              <el-button type="warning" @click="$router.push('/agents')">
                Configure Agents
              </el-button>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useAdminStore} from '../stores/admin'

const adminStore = useAdminStore()

const status = ref({
  channels: {},
  sessions: {},
  agents: {}
})

onMounted(async () => {
  await adminStore.fetchStatus()
  status.value = adminStore.status
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  text-align: center;
}

.stat {
  padding: 20px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-value.success {
  color: #67C23A;
}

.stat-desc {
  font-size: 12px;
  color: #C0C4CC;
}
</style>
