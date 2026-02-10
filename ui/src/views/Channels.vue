<template>
  <div class="channels">
    <h2>Channel Management</h2>
    
    <el-table :data="channels" style="margin-top: 20px">
      <el-table-column prop="id" label="Channel ID" width="150" />
      <el-table-column prop="name" label="Name" width="200" />
      <el-table-column label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.running ? 'success' : 'info'">
            {{ row.running ? 'Running' : 'Stopped' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Connection Status" />
      <el-table-column label="Actions" width="200">
        <template #default="{ row }">
          <el-button 
            :type="row.running ? 'danger' : 'primary'" 
            size="small"
            @click="toggleChannel(row)"
          >
            {{ row.running ? 'Stop' : 'Start' }}
          </el-button>
          <el-button size="small" @click="viewConfig(row)">
            Config
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useAdminStore} from '../stores/admin'

const adminStore = useAdminStore()
const channels = ref([])

onMounted(async () => {
  await adminStore.fetchChannels()
  channels.value = adminStore.channels
})

function toggleChannel(channel) {
  console.log('Toggle channel:', channel.id)
  // TODO: Implement channel start/stop
}

function viewConfig(channel) {
  console.log('View config for:', channel.id)
  // TODO: Open config dialog
}
</script>

<style scoped>
.channels {
  padding: 20px;
}
</style>
