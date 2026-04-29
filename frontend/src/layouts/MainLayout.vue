<template>
  <div class="min-h-screen bg-[radial-gradient(circle_at_top,#fff_0%,#f8fafc_52%,#f1f5f9_100%)] text-slate-700">
    <header class="sticky top-0 z-30 border-b border-brand/10 bg-white/92 backdrop-blur-xl">
      <div class="mx-auto flex w-full max-w-7xl items-center justify-between gap-6 px-4 py-3 sm:px-6 lg:px-8">
        <RouterLink to="/" class="flex min-w-0 items-center gap-3">
          <div class="flex h-14 w-14 items-center justify-center overflow-hidden rounded-full border border-brand/15 bg-[linear-gradient(135deg,#fff_0%,#f7eae8_100%)] shadow-soft">
            <img :src="schoolBadgeUrl" alt="河北工程大学校徽" class="h-full w-full object-cover" />
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

    <el-dialog v-model="authDialogVisible" width="460px" :title="authMode === 'login' ? '登录系统' : '注册账号'">
      <div class="mb-5 inline-flex rounded-xl bg-brand/5 p-1">
        <button
          type="button"
          class="rounded-lg px-4 py-2 text-sm font-semibold transition"
          :class="authMode === 'login' ? 'bg-white text-brand shadow-sm' : 'text-slate-500'"
          @click="switchAuthMode('login')"
        >
          登录
        </button>
        <button
          type="button"
          class="rounded-lg px-4 py-2 text-sm font-semibold transition"
          :class="authMode === 'register' ? 'bg-white text-brand shadow-sm' : 'text-slate-500'"
          @click="switchAuthMode('register')"
        >
          注册
        </button>
      </div>

      <el-form
        v-if="authMode === 'login'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        status-icon
        @keyup.enter="handleLoginSubmit"
      >
        <el-form-item label="账号" prop="username" :error="loginFieldErrors.username">
          <el-input
            v-model="loginForm.username"
            maxlength="32"
            placeholder="请输入账号"
            @input="clearFieldError(loginFieldErrors, 'username')"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" :error="loginFieldErrors.password">
          <el-input
            v-model="loginForm.password"
            type="password"
            show-password
            maxlength="32"
            placeholder="请输入密码"
            @input="clearFieldError(loginFieldErrors, 'password')"
          />
        </el-form-item>
        <p class="mb-4 text-xs leading-6 text-slate-400">账号不能包含前后空格，密码至少 8 位。</p>
        <el-button
          type="danger"
          class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark"
          :loading="loginSubmitting"
          @click="handleLoginSubmit"
        >
          立即登录
        </el-button>
      </el-form>

      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        status-icon
        @keyup.enter="handleRegisterSubmit"
      >
        <el-form-item label="账号" prop="username" :error="registerFieldErrors.username">
          <el-input
            v-model="registerForm.username"
            maxlength="32"
            placeholder="请输入账号（建议使用qq号）"
            @input="clearFieldError(registerFieldErrors, 'username')"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" :error="registerFieldErrors.password">
          <el-input
            v-model="registerForm.password"
            type="password"
            show-password
            maxlength="32"
            placeholder="请输入密码"
            @input="clearFieldError(registerFieldErrors, 'password')"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" :error="registerFieldErrors.confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            show-password
            maxlength="32"
            placeholder="请再次输入密码"
            @input="clearFieldError(registerFieldErrors, 'confirmPassword')"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname" :error="registerFieldErrors.nickname">
          <el-input
            v-model="registerForm.nickname"
            maxlength="32"
            placeholder="请输入昵称"
            @input="clearFieldError(registerFieldErrors, 'nickname')"
          />
        </el-form-item>
        <el-form-item label="身份" prop="role" :error="registerFieldErrors.role">
          <el-radio-group v-model="registerForm.role" @change="clearFieldError(registerFieldErrors, 'role')">
            <el-radio :label="1">我是新生</el-radio>
            <el-radio :label="2">我是老生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="入学年份（可选）" prop="admissionYear" :error="registerFieldErrors.admissionYear">
          <el-input-number
            v-model="registerForm.admissionYear"
            :min="2000"
            :max="2100"
            class="!w-full"
            @change="clearFieldError(registerFieldErrors, 'admissionYear')"
          />
        </el-form-item>
        <div class="mb-4 rounded-2xl bg-slate-50 px-4 py-3 text-xs leading-6 text-slate-500">
          账号 4 到 32 位<br />
          密码至少 8 位<br />
          昵称最多 32 位<br />
          账号不能包含前后空格
        </div>
        <el-button
          type="danger"
          class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark"
          :loading="registerSubmitting"
          @click="handleRegisterSubmit"
        >
          注册账号
        </el-button>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAppShell } from '../stores/appShell'

const route = useRoute()
const schoolBadgeUrl = '/pictures/xiaohui.png'
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
  submitLogin: submitLoginRequest,
  submitRegister: submitRegisterRequest,
  handleLogout
} = useAppShell()

const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loginFieldErrors = reactive({
  username: '',
  password: ''
})
const registerFieldErrors = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  role: '',
  admissionYear: ''
})

const loginRules = {
  username: [{ validator: validateUsername, trigger: ['blur', 'change'] }],
  password: [{ validator: validateLoginPassword, trigger: ['blur', 'change'] }]
}

const registerRules = {
  username: [{ validator: validateUsername, trigger: ['blur', 'change'] }],
  password: [{ validator: validateRegisterPassword, trigger: ['blur', 'change'] }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: ['blur', 'change'] }],
  nickname: [{ validator: validateNickname, trigger: ['blur', 'change'] }],
  role: [{ validator: validateRole, trigger: ['blur', 'change'] }],
  admissionYear: [{ validator: validateAdmissionYear, trigger: ['blur', 'change'] }]
}

const visibleNavItems = computed(() =>
  navItems.filter((item) => !item.adminOnly || [8, 9].includes(currentUser.value?.role))
)

onMounted(async () => {
  await ensureShellReady()
})

watch(
  () => authDialogVisible.value,
  async (visible) => {
    if (!visible) {
      resetFieldErrors(loginFieldErrors)
      resetFieldErrors(registerFieldErrors)
      return
    }
    await nextTick()
    loginFormRef.value?.clearValidate()
    registerFormRef.value?.clearValidate()
  }
)

watch(
  () => authMode.value,
  async () => {
    resetFieldErrors(loginFieldErrors)
    resetFieldErrors(registerFieldErrors)
    await nextTick()
    loginFormRef.value?.clearValidate()
    registerFormRef.value?.clearValidate()
  }
)

function isNavActive(targetPath) {
  if (targetPath === '/forum') {
    return route.path === '/forum' || route.path.startsWith('/forum/')
  }
  return route.path === targetPath
}

function switchAuthMode(mode) {
  authMode.value = mode
}

function resetFieldErrors(target) {
  Object.keys(target).forEach((key) => {
    target[key] = ''
  })
}

function clearFieldError(target, field) {
  target[field] = ''
}

function validateUsername(_rule, value, callback) {
  if (!value) {
    callback(new Error('请输入账号（建议使用qq号）'))
    return
  }
  if (value !== value.trim()) {
    callback(new Error('账号不能包含前后空格'))
    return
  }
  if (value.length < 4 || value.length > 32) {
    callback(new Error('账号需为 4 到 32 位'))
    return
  }
  callback()
}

function validateLoginPassword(_rule, value, callback) {
  if (!value) {
    callback(new Error('请输入密码'))
    return
  }
  if (value.length < 8 || value.length > 32) {
    callback(new Error('密码长度需为 8 到 32 位'))
    return
  }
  callback()
}

function validateRegisterPassword(_rule, value, callback) {
  if (!value) {
    callback(new Error('请输入密码'))
    return
  }
  if (value.length < 8 || value.length > 32) {
    callback(new Error('密码长度需为 8 到 32 位'))
    return
  }
  callback()
}

function validateConfirmPassword(_rule, value, callback) {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

function validateNickname(_rule, value, callback) {
  if (!value || !value.trim()) {
    callback(new Error('请输入昵称'))
    return
  }
  if (value.trim().length > 32) {
    callback(new Error('昵称最多 32 位'))
    return
  }
  callback()
}

function validateRole(_rule, value, callback) {
  if (![1, 2].includes(value)) {
    callback(new Error('请选择身份'))
    return
  }
  callback()
}

function validateAdmissionYear(_rule, value, callback) {
  if (value === null || value === undefined || value === '') {
    callback()
    return
  }
  if (!Number.isInteger(value) || value < 2000 || value > 2100) {
    callback(new Error('入学年份需在 2000 到 2100 之间'))
    return
  }
  callback()
}

function normalizeFieldMessage(message, field) {
  return message.startsWith(`${field} `) ? message.slice(field.length + 1) : message
}

function applyLoginFieldError(message) {
  if (message.startsWith('username ')) {
    loginFieldErrors.username = normalizeFieldMessage(message, 'username')
    return true
  }
  if (message.startsWith('password ')) {
    loginFieldErrors.password = normalizeFieldMessage(message, 'password')
    return true
  }
  if (message.includes('账号或密码错误') || message.includes('用户名或密码错误')) {
    loginFieldErrors.password = '账号或密码错误'
    return true
  }
  if (message.includes('账号')) {
    loginFieldErrors.username = message
    return true
  }
  if (message.includes('密码')) {
    loginFieldErrors.password = message
    return true
  }
  return false
}

function applyRegisterFieldError(message) {
  if (message.startsWith('username ')) {
    registerFieldErrors.username = normalizeFieldMessage(message, 'username')
    return true
  }
  if (message.startsWith('password ')) {
    registerFieldErrors.password = normalizeFieldMessage(message, 'password')
    return true
  }
  if (message.startsWith('nickname ')) {
    registerFieldErrors.nickname = normalizeFieldMessage(message, 'nickname')
    return true
  }
  if (message.startsWith('role ')) {
    registerFieldErrors.role = normalizeFieldMessage(message, 'role')
    return true
  }
  if (message.startsWith('admissionYear ')) {
    registerFieldErrors.admissionYear = normalizeFieldMessage(message, 'admissionYear')
    return true
  }
  if (message.includes('两次输入的密码不一致')) {
    registerFieldErrors.confirmPassword = message
    return true
  }
  if (message.includes('账号')) {
    registerFieldErrors.username = message
    return true
  }
  if (message.includes('密码')) {
    registerFieldErrors.password = message
    return true
  }
  if (message.includes('昵称')) {
    registerFieldErrors.nickname = message
    return true
  }
  if (message.includes('身份') || message.includes('角色')) {
    registerFieldErrors.role = message
    return true
  }
  if (message.includes('入学年份')) {
    registerFieldErrors.admissionYear = message
    return true
  }
  return false
}

async function handleLoginSubmit() {
  resetFieldErrors(loginFieldErrors)
  const isValid = await loginFormRef.value.validate().then(() => true).catch(() => false)
  if (!isValid) return
  try {
    await submitLoginRequest({
      username: loginForm.username.trim(),
      password: loginForm.password
    })
  } catch (error) {
    if (!applyLoginFieldError(error.message)) {
      ElMessage.error(error.message)
    }
  }
}

async function handleRegisterSubmit() {
  resetFieldErrors(registerFieldErrors)
  const isValid = await registerFormRef.value.validate().then(() => true).catch(() => false)
  if (!isValid) return
  try {
    await submitRegisterRequest({
      username: registerForm.username.trim(),
      password: registerForm.password,
      nickname: registerForm.nickname.trim(),
      role: registerForm.role,
      admissionYear: registerForm.admissionYear
    })
  } catch (error) {
    if (!applyRegisterFieldError(error.message)) {
      ElMessage.error(error.message)
    }
  }
}
</script>
