import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchCurrentUser, login, logout, register } from '../api/auth'

const currentUser = ref(null)
const healthStatus = ref('CHECKING')
const authDialogVisible = ref(false)
const authMode = ref('login')
const initialized = ref(false)
const loginSubmitting = ref(false)
const registerSubmitting = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  nickname: ''
})

const currentUserRoleLabel = computed(() => {
  const role = currentUser.value?.role
  if (role === 9) return '超级管理员'
  if (role === 8) return '管理员'
  if (role === 2) return '老生'
  if (role === 1) return '新生'
  return '游客'
})

async function loadHealth() {
  try {
    const response = await fetch('/actuator/health')
    const data = await response.json()
    healthStatus.value = data.status || 'UNKNOWN'
  } catch (_error) {
    healthStatus.value = 'DOWN'
  }
}

async function loadCurrentUser() {
  const token = localStorage.getItem('cfh_token')
  if (!token) {
    currentUser.value = null
    return
  }
  try {
    currentUser.value = await fetchCurrentUser()
  } catch (_error) {
    localStorage.removeItem('cfh_token')
    currentUser.value = null
  }
}

async function refreshCurrentUser() {
  await loadCurrentUser()
}

async function ensureShellReady() {
  if (initialized.value) {
    return
  }
  await Promise.all([loadHealth(), loadCurrentUser()])
  initialized.value = true
}

function openAuth(mode = 'login') {
  authMode.value = mode
  authDialogVisible.value = true
}

async function submitLogin() {
  loginSubmitting.value = true
  try {
    const result = await login(loginForm)
    const tokenValue = result.tokenPrefix ? `${result.tokenPrefix} ${result.tokenValue}` : result.tokenValue
    localStorage.setItem('cfh_token', tokenValue)
    currentUser.value = result.user
    authDialogVisible.value = false
    loginForm.password = ''
    ElMessage.success(`登录成功，欢迎你 ${result.user.nickname}`)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loginSubmitting.value = false
  }
}

async function submitRegister() {
  registerSubmitting.value = true
  try {
    const user = await register(registerForm)
    ElMessage.success(`注册成功：${user.nickname}`)
    loginForm.username = registerForm.username
    loginForm.password = registerForm.password
    registerForm.username = ''
    registerForm.password = ''
    registerForm.nickname = ''
    authMode.value = 'login'
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    registerSubmitting.value = false
  }
}

async function handleLogout() {
  try {
    await logout()
  } catch (_error) {
  } finally {
    localStorage.removeItem('cfh_token')
    currentUser.value = null
    ElMessage.success('已退出登录')
  }
}

export function useAppShell() {
  return {
    currentUser,
    healthStatus,
    authDialogVisible,
    authMode,
    loginSubmitting,
    registerSubmitting,
    loginForm,
    registerForm,
    currentUserRoleLabel,
    ensureShellReady,
    openAuth,
    submitLogin,
    submitRegister,
    handleLogout,
    refreshCurrentUser
  }
}
