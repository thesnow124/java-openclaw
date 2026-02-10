import {createRouter, createWebHistory} from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'dashboard',
    component: () => import('../views/Dashboard.vue')
  },
  {
    path: '/channels',
    name: 'channels',
    component: () => import('../views/Channels.vue')
  },
  {
    path: '/sessions',
    name: 'sessions',
    component: () => import('../views/Sessions.vue')
  },
  {
    path: '/agents',
    name: 'agents',
    component: () => import('../views/Agents.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
