import {defineStore} from 'pinia'
import {ref} from 'vue'
import api from '../api/admin'

export const useAdminStore = defineStore('admin', () => {
  const status = ref({
    channels: { running: 0, total: 0 },
    sessions: { total: 0, active: 0 },
    agents: { total: 0 }
  })
  
  const channels = ref([])
  const sessions = ref([])
  const agents = ref([])
  
  async function fetchStatus() {
    try {
      const response = await api.getStatus()
      status.value = response
    } catch (error) {
      console.error('Failed to fetch status:', error)
    }
  }
  
  async function fetchChannels() {
    try {
      const response = await api.getChannelStats()
      channels.value = response.channels || []
    } catch (error) {
      console.error('Failed to fetch channels:', error)
    }
  }
  
  async function fetchSessions() {
    try {
      const response = await api.getSessionStats()
      sessions.value = response
    } catch (error) {
      console.error('Failed to fetch sessions:', error)
    }
  }
  
  async function fetchAgents() {
    try {
      // Would call agent API
      agents.value = []
    } catch (error) {
      console.error('Failed to fetch agents:', error)
    }
  }
  
  return {
    status,
    channels,
    sessions,
    agents,
    fetchStatus,
    fetchChannels,
    fetchSessions,
    fetchAgents
  }
})
