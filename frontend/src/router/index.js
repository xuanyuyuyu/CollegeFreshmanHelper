import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'
import AssistantPage from '../pages/AssistantPage.vue'
import ForumPage from '../pages/ForumPage.vue'
import ForumDetailPage from '../pages/ForumDetailPage.vue'
import GuidePage from '../pages/GuidePage.vue'
import MePage from '../pages/MePage.vue'

const routes = [
  { path: '/', name: 'home', component: HomePage },
  { path: '/assistant', name: 'assistant', component: AssistantPage },
  { path: '/forum', name: 'forum', component: ForumPage },
  { path: '/forum/:postId', name: 'forum-detail', component: ForumDetailPage },
  { path: '/guide', name: 'guide', component: GuidePage },
  { path: '/me', name: 'me', component: MePage }
]

export default createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0, behavior: 'smooth' }
  }
})
