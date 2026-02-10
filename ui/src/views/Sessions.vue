<template>
  <div class="sessions">
    <h2>Session Management</h2>
    
    <el-card style="margin-top: 20px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Total Sessions">
          {{ stats.total }}
        </el-descriptions-item>
        <el-descriptions-item label="Active Sessions">
          {{ stats.active }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <el-table :data="[]" style="margin-top: 20px">
      <el-table-column prop="id" label="Session ID" />
      <el-table-column prop="channel" label="Channel" />
      <el-table-column prop="user" label="User" />
      <el-table-column prop="messages" label="Messages" />
      <el-table-column prop="lastActivity" label="Last Activity" />
      <el-table-column label="Actions">
        <template #default="{ row }">
          <el-button size="small" type="primary">View</el-button>
          <el-button size="small" type="danger">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useAdminStore} from '../stores/admin'

const adminStore = useAdminStore()
const stats = ref({})

onMounted(async () => {
  await adminStore.fetchSessions()
  stats.value = adminStore.sessions
})
</script>

<style scoped>
.sessions {
  padding: 20px;
}
</style>
