<template>
  <div class="min-h-screen bg-[radial-gradient(circle_at_top,#fff_0%,#f8fafc_52%,#f1f5f9_100%)] text-slate-700">
    <header class="sticky top-0 z-30 border-b border-brand/10 bg-white/92 backdrop-blur-xl">
      <div class="mx-auto flex w-full max-w-7xl items-center justify-between gap-6 px-4 py-3 sm:px-6 lg:px-8">
        <RouterLink to="/" class="flex min-w-0 items-center gap-3">
          <div class="flex h-14 w-14 items-center justify-center rounded-full border border-brand/15 bg-[linear-gradient(135deg,#fff_0%,#f7eae8_100%)] text-lg font-bold text-brand shadow-soft">
            H
          </div>
          <div class="min-w-0">
            <div class="truncate text-xl font-bold tracking-tight text-brand sm:text-2xl">河北工程大学</div>
            <div class="truncate text-sm tracking-wide text-slate-500">新生入学帮助系统</div>
          </div>
        </RouterLink>

        <nav class="hidden items-center gap-1 rounded-2xl bg-slate-100/75 p-1.5 lg:flex">
          <template v-for="item in visibleNavItems" :key="item.label">
            <a
              v-if="item.href"
              :href="item.href"
              target="_blank"
              rel="noopener noreferrer"
              class="rounded-xl px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-white hover:text-brand"
            >
              {{ item.label }}
            </a>
            <RouterLink
              v-else
              :to="item.to"
              class="rounded-xl px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-white hover:text-brand"
              :class="{ 'bg-white text-brand shadow-sm': isNavActive(item.to) }"
            >
              {{ item.label }}
            </RouterLink>
          </template>
        </nav>

        <div class="flex items-center gap-3">
          <span
            class="hidden rounded-full px-3 py-1 text-xs font-semibold sm:inline-flex"
            :class="healthStatus === 'UP' ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'"
          >
            {{ healthStatus === 'UP' ? '服务正常' : '检测中' }}
          </span>
          <button
            v-if="!currentUser"
            type="button"
            class="rounded-2xl border border-brand/20 px-4 py-2 text-sm font-semibold text-brand transition hover:bg-brand hover:text-white"
            @click="openAuth('login')"
          >
            登录 / 注册
          </button>
          <div v-else class="flex items-center gap-3">
            <span class="hidden text-sm text-slate-500 md:inline">你好，{{ currentUser.nickname }}</span>
            <button
              type="button"
              class="rounded-2xl border border-brand/20 px-4 py-2 text-sm font-semibold text-brand transition hover:bg-brand hover:text-white"
              @click="handleLogout"
            >
              退出
            </button>
          </div>
        </div>
      </div>
    </header>

    <main>
      <slot />
    </main>

    <el-dialog v-model="authDialogVisible" width="420px" :title="authMode === 'login' ? '登录系统' : '注册账号'">
      <div class="mb-5 inline-flex rounded-xl bg-brand/5 p-1">
        <button
          type="button"
          class="rounded-lg px-4 py-2 text-sm font-semibold transition"
          :class="authMode === 'login' ? 'bg-white text-brand shadow-sm' : 'text-slate-500'"
          @click="authMode = 'login'"
        >
          登录
        </button>
        <button
          type="button"
          class="rounded-lg px-4 py-2 text-sm font-semibold transition"
          :class="authMode === 'register' ? 'bg-white text-brand shadow-sm' : 'text-slate-500'"
          @click="authMode = 'register'"
        >
          注册
        </button>
      </div>

      <div v-if="authMode === 'login'" class="space-y-4">
        <el-input v-model="loginForm.username" placeholder="用户名" />
        <el-input v-model="loginForm.password" type="password" show-password placeholder="密码" />
        <el-button
          type="danger"
          class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark"
          :loading="loginSubmitting"
          @click="submitLogin"
        >
          立即登录
        </el-button>
      </div>

      <div v-else class="space-y-4">
        <el-input v-model="registerForm.username" placeholder="用户名" />
        <el-input v-model="registerForm.password" type="password" show-password placeholder="至少 6 位密码" />
        <el-input v-model="registerForm.nickname" placeholder="昵称" />
        <el-button
          type="danger"
          class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark"
          :loading="registerSubmitting"
          @click="submitRegister"
        >
          注册账号
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useAppShell } from '../stores/appShell'

const route = useRoute()
const navItems = [
  { to: '/', label: '首页' },
  { to: '/assistant', label: '智能问答' },
  { to: '/forum', label: '论坛交流' },
  { href: 'https://www.720yun.com/t/2avktm1qs2m?scene_id=75861491', label: '全景校园' },
  { to: '/guide', label: '入学指南' },
  { to: '/me', label: '我的' },
  { to: '/admin', label: '后台管理', adminOnly: true }
]

const {
  currentUser,
  healthStatus,
  authDialogVisible,
  authMode,
  loginSubmitting,
  registerSubmitting,
  loginForm,
  registerForm,
  ensureShellReady,
  openAuth,
  submitLogin,
  submitRegister,
  handleLogout
} = useAppShell()

const visibleNavItems = computed(() =>
  navItems.filter((item) => !item.adminOnly || [8, 9].includes(currentUser.value?.role))
)

onMounted(async () => {
  await ensureShellReady()
})

function isNavActive(targetPath) {
  if (targetPath === '/forum') {
    return route.path === '/forum' || route.path.startsWith('/forum/')
  }
  return route.path === targetPath
}
</script>
