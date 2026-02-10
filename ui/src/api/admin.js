import axios from 'axios'

const api = axios.create({
  baseURL: '/api/admin',
  timeout: 10000
})

export default {
  getStatus() {
    return api.get('/status').then(res => res.data)
  },
  
  getChannelStats() {
    return api.get('/channels/stats').then(res => res.data)
  },
  
  getSessionStats() {
    return api.get('/sessions/stats').then(res => res.data)
  },
  
  getConfig() {
    return api.get('/config').then(res => res.data)
  }
}
