import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  banUser,
  createAdminUser,
  createKnowledge,
  deleteAdminPost,
  deleteAdminReply,
  fetchAdminLogs,
  fetchAdminPosts,
  fetchAdminReplies,
  fetchAdminUsers,
  fetchGrantedTitles,
  fetchKnowledgeList,
  fetchTitles,
  grantTitle,
  revokeTitle,
  unbanUser,
  updateAdminPostVisibility,
  updateAdminReplyVisibility,
  updateAdminUserStatus,
  updateKnowledgeStatus
} from '../api/admin'

const LIST_FETCH_SIZE = 100

export function useAdminConsole(appShell) {
  const router = useRouter()
  const { currentUser } = appShell

  const activeTab = ref('posts')
  const loading = reactive({
    any: false,
    createAdmin: false
  })

  const adminAccounts = ref([])
  const adminPosts = ref([])
  const adminReplies = ref([])
  const siteUsers = ref([])
  const titleOptions = ref([])
  const grantedTitles = ref([])
  const knowledgeList = ref([])
  const adminLogs = ref([])
  const postTotal = ref(0)
  const replyTotal = ref(0)
  const userTotal = ref(0)

  const adminFilters = reactive({ keyword: '', status: null })
  const postFilters = reactive({ keyword: '', status: null, visibility: null })
  const replyFilters = reactive({ keyword: '', postId: '', status: null, visibility: null })
  const userFilters = reactive({ keyword: '', role: null, status: null })
  const knowledgeFilters = reactive({ keyword: '', status: null, category: '' })
  const logFilters = reactive({ targetType: null, operationType: '' })

  const adminCreateForm = reactive({ username: '', password: '', nickname: '' })
  const grantTitleForm = reactive({ username: '', titleId: null, wearing: true, remark: '' })
  const knowledgeForm = reactive({ questionText: '', answerText: '', category: '', rewardPoints: '' })

  const isAdmin = computed(() => [8, 9].includes(currentUser.value?.role))
  const isSuperAdmin = computed(() => currentUser.value?.role === 9)
  const adminRoleLabel = computed(() => (isSuperAdmin.value ? '超级管理员权限' : '管理员权限'))

  function roleLabel(role) {
    if (role === 9) return '超级管理员'
    if (role === 8) return '管理员'
    if (role === 2) return '老生'
    if (role === 1) return '新生'
    return '用户'
  }

  function postStatusLabel(status) {
    if (status === 0) return '审核中'
    if (status === 1) return '已发布'
    if (status === 2) return '已驳回'
    if (status === 3) return '已隐藏'
    return '未知状态'
  }

  function knowledgeStatusLabel(status) {
    if (status === 1) return '待向量化'
    if (status === 2) return '已启用'
    if (status === 4) return '已下线'
    return '其他状态'
  }

  function formatTime(value) {
    if (!value) return '刚刚'
    return new Date(value).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  function targetTypeLabel(targetType) {
    if (targetType === 1) return '用户'
    if (targetType === 2) return '帖子'
    if (targetType === 3) return '评论'
    if (targetType === 4) return '知识库'
    if (targetType === 6) return '头衔'
    return '其他'
  }

  function canBanUser(user) {
    if (!user) return false
    if (user.role === 9) return false
    if (isSuperAdmin.value) return true
    return isAdmin.value && user.role < 8
  }

  function goToPost(postId) {
    router.push(`/forum/${postId}`)
  }

  async function loadAdminAccounts() {
    if (!isSuperAdmin.value) return
    const adminPage = await fetchAdminUsers({
      pageNum: 1,
      pageSize: LIST_FETCH_SIZE,
      role: 8,
      status: adminFilters.status,
      keyword: adminFilters.keyword || undefined
    })
    const superAdminPage = await fetchAdminUsers({
      pageNum: 1,
      pageSize: LIST_FETCH_SIZE,
      role: 9,
      status: adminFilters.status,
      keyword: adminFilters.keyword || undefined
    })
    adminAccounts.value = [...(superAdminPage.records || []), ...(adminPage.records || [])]
  }

  async function loadAdminPosts() {
    const page = await fetchAdminPosts({
      pageNum: 1,
      pageSize: LIST_FETCH_SIZE,
      keyword: postFilters.keyword || undefined,
      status: postFilters.status,
      visibility: postFilters.visibility
    })
    adminPosts.value = page.records || []
    postTotal.value = page.total ?? adminPosts.value.length
  }

  async function loadAdminReplies() {
    const page = await fetchAdminReplies({
      pageNum: 1,
      pageSize: LIST_FETCH_SIZE,
      keyword: replyFilters.keyword || undefined,
      postId: replyFilters.postId ? Number(replyFilters.postId) : undefined,
      status: replyFilters.status,
      visibility: replyFilters.visibility
    })
    adminReplies.value = page.records || []
    replyTotal.value = page.total ?? adminReplies.value.length
  }

  async function loadUserList() {
    const page = await fetchAdminUsers({
      pageNum: 1,
      pageSize: LIST_FETCH_SIZE,
      keyword: userFilters.keyword || undefined,
      role: userFilters.role,
      status: userFilters.status
    })
    siteUsers.value = page.records || []
    userTotal.value = page.total ?? siteUsers.value.length
  }

  async function loadTitleOptions() {
    titleOptions.value = await fetchTitles()
  }

  async function loadGrantedTitles() {
    const page = await fetchGrantedTitles({ pageNum: 1, pageSize: LIST_FETCH_SIZE })
    grantedTitles.value = page.records || []
  }

  async function loadKnowledgeList() {
    const page = await fetchKnowledgeList({
      pageNum: 1,
      pageSize: LIST_FETCH_SIZE,
      keyword: knowledgeFilters.keyword || undefined,
      status: knowledgeFilters.status,
      category: knowledgeFilters.category || undefined
    })
    knowledgeList.value = page.records || []
  }

  async function loadAdminLogs() {
    const page = await fetchAdminLogs({
      pageNum: 1,
      pageSize: LIST_FETCH_SIZE,
      targetType: logFilters.targetType,
      operationType: logFilters.operationType || undefined
    })
    adminLogs.value = page.records || []
  }

  async function reloadCurrentTab() {
    if (!isAdmin.value) return
    loading.any = true
    try {
      if (activeTab.value === 'admins' && isSuperAdmin.value) {
        await loadAdminAccounts()
      } else if (activeTab.value === 'posts') {
        await loadAdminPosts()
      } else if (activeTab.value === 'replies') {
        await loadAdminReplies()
      } else if (activeTab.value === 'users') {
        await loadUserList()
      } else if (activeTab.value === 'ops') {
        await Promise.all([loadTitleOptions(), loadGrantedTitles(), loadKnowledgeList(), loadAdminLogs()])
      }
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      loading.any = false
    }
  }

  async function submitCreateAdmin() {
    if (!adminCreateForm.username.trim() || !adminCreateForm.password.trim() || !adminCreateForm.nickname.trim()) {
      ElMessage.warning('请完整填写管理员账号信息')
      return
    }
    loading.createAdmin = true
    try {
      await createAdminUser({
        username: adminCreateForm.username.trim(),
        password: adminCreateForm.password.trim(),
        nickname: adminCreateForm.nickname.trim()
      })
      adminCreateForm.username = ''
      adminCreateForm.password = ''
      adminCreateForm.nickname = ''
      ElMessage.success('管理员账号已创建')
      await loadAdminAccounts()
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      loading.createAdmin = false
    }
  }

  async function toggleAdminStatus(user) {
    try {
      await updateAdminUserStatus(user.id, user.status === 1 ? 0 : 1)
      ElMessage.success(user.status === 1 ? '管理员账号已禁用' : '管理员账号已启用')
      await loadAdminAccounts()
    } catch (error) {
      ElMessage.error(error.message)
    }
  }

  async function togglePostVisibility(post) {
    try {
      const { value } = await ElMessageBox.prompt(
        post.visibility === 1 ? `确认隐藏帖子《${post.title}》？` : `确认恢复帖子《${post.title}》为可见？`,
        post.visibility === 1 ? '隐藏帖子' : '恢复帖子',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          inputPlaceholder: '操作原因（可选）'
        }
      )
      await updateAdminPostVisibility(post.id, {
        visible: post.visibility !== 1,
        reason: value || (post.visibility === 1 ? '后台隐藏帖子' : '后台恢复帖子')
      })
      ElMessage.success(post.visibility === 1 ? '帖子已隐藏' : '帖子已恢复可见')
      await loadAdminPosts()
    } catch (error) {
      if (error === 'cancel' || error === 'close') return
      ElMessage.error(error.message || '操作失败')
    }
  }

  async function confirmDeletePost(post) {
    try {
      const { value } = await ElMessageBox.prompt(`确认删除帖子《${post.title}》？可填写删除原因。`, '删除帖子', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        inputPlaceholder: '删除原因（可选）'
      })
      await deleteAdminPost(post.id, { reason: value || '后台手动删除帖子' })
      ElMessage.success('帖子已删除')
      await loadAdminPosts()
    } catch (error) {
      if (error === 'cancel' || error === 'close') return
      ElMessage.error(error.message || '删除失败')
    }
  }

  async function toggleReplyVisibility(reply) {
    try {
      const { value } = await ElMessageBox.prompt(
        reply.visibility === 1 ? '确认隐藏该评论？' : '确认恢复该评论为可见？',
        reply.visibility === 1 ? '隐藏评论' : '恢复评论',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          inputPlaceholder: '操作原因（可选）'
        }
      )
      await updateAdminReplyVisibility(reply.id, {
        visible: reply.visibility !== 1,
        reason: value || (reply.visibility === 1 ? '后台隐藏评论' : '后台恢复评论')
      })
      ElMessage.success(reply.visibility === 1 ? '评论已隐藏' : '评论已恢复可见')
      await loadAdminReplies()
    } catch (error) {
      if (error === 'cancel' || error === 'close') return
      ElMessage.error(error.message || '操作失败')
    }
  }

  async function confirmDeleteReply(reply) {
    try {
      const { value } = await ElMessageBox.prompt('确认删除该评论？可填写删除原因。', '删除评论', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        inputPlaceholder: '删除原因（可选）'
      })
      await deleteAdminReply(reply.id, { reason: value || '后台手动删除评论' })
      ElMessage.success('评论已删除')
      await loadAdminReplies()
    } catch (error) {
      if (error === 'cancel' || error === 'close') return
      ElMessage.error(error.message || '删除失败')
    }
  }

  async function openBanPrompt(user) {
    try {
      const { value } = await ElMessageBox.prompt(`请输入封禁用户 ${user.nickname} 的原因`, '封禁用户', {
        confirmButtonText: '确认封禁',
        cancelButtonText: '取消',
        inputPlaceholder: '封禁原因'
      })
      await banUser(user.id, { reason: value || '后台手动封禁', banExpireAt: null })
      ElMessage.success('用户已封禁')
      await loadUserList()
    } catch (error) {
      if (error === 'cancel' || error === 'close') return
      ElMessage.error(error.message || '封禁失败')
    }
  }

  async function handleUnbanUser(user) {
    try {
      await unbanUser(user.id)
      ElMessage.success('用户已解封')
      await loadUserList()
    } catch (error) {
      ElMessage.error(error.message)
    }
  }

  async function submitGrantTitle() {
    const username = String(grantTitleForm.username || '').trim()
    if (!username || !grantTitleForm.titleId) {
      ElMessage.warning('请填写账号并选择头衔')
      return
    }
    try {
      await grantTitle({
        username,
        titleId: grantTitleForm.titleId,
        wearing: grantTitleForm.wearing,
        remark: grantTitleForm.remark
      })
      ElMessage.success('头衔已授予')
      grantTitleForm.username = ''
      grantTitleForm.titleId = null
      grantTitleForm.wearing = true
      grantTitleForm.remark = ''
      await Promise.all([loadGrantedTitles(), loadAdminLogs()])
    } catch (error) {
      ElMessage.error(error.message)
    }
  }

  async function revokeGrantedTitle(grant) {
    try {
      const { value } = await ElMessageBox.prompt(`确认撤销 ${grant.userNickname} 的头衔「${grant.titleName}」？`, '撤销头衔', {
        confirmButtonText: '确认撤销',
        cancelButtonText: '取消',
        inputPlaceholder: '撤销原因（可选）'
      })
      await revokeTitle(grant.id, { reason: value || '后台撤销头衔' })
      ElMessage.success('头衔已撤销')
      await Promise.all([loadGrantedTitles(), loadAdminLogs()])
    } catch (error) {
      if (error === 'cancel' || error === 'close') return
      ElMessage.error(error.message)
    }
  }

  async function submitKnowledge() {
    if (!knowledgeForm.questionText.trim() || !knowledgeForm.answerText.trim()) {
      ElMessage.warning('请填写知识库问题和答案')
      return
    }
    try {
      await createKnowledge({
        questionText: knowledgeForm.questionText.trim(),
        answerText: knowledgeForm.answerText.trim(),
        category: knowledgeForm.category.trim() || null,
        rewardPoints: knowledgeForm.rewardPoints ? Number(knowledgeForm.rewardPoints) : 0
      })
      ElMessage.success('知识库条目已创建')
      knowledgeForm.questionText = ''
      knowledgeForm.answerText = ''
      knowledgeForm.category = ''
      knowledgeForm.rewardPoints = ''
      await Promise.all([loadKnowledgeList(), loadAdminLogs()])
    } catch (error) {
      ElMessage.error(error.message)
    }
  }

  async function changeKnowledgeStatus(item, status) {
    try {
      const { value } = await ElMessageBox.prompt(
        `确认将知识条目改为「${knowledgeStatusLabel(status)}」？`,
        '更新知识条目状态',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          inputPlaceholder: '变更原因（可选）'
        }
      )
      await updateKnowledgeStatus(item.id, {
        status,
        reason: value || `后台更新为${knowledgeStatusLabel(status)}`
      })
      ElMessage.success('知识条目状态已更新')
      await Promise.all([loadKnowledgeList(), loadAdminLogs()])
    } catch (error) {
      if (error === 'cancel' || error === 'close') return
      ElMessage.error(error.message)
    }
  }

  watch(
    () => activeTab.value,
    async () => {
      if (isAdmin.value) {
        await reloadCurrentTab()
      }
    }
  )

  watch(
    () => currentUser.value?.id,
    async (nextId) => {
      if (nextId && isAdmin.value) {
        await reloadCurrentTab()
        return
      }
      adminAccounts.value = []
      adminPosts.value = []
      adminReplies.value = []
      siteUsers.value = []
      titleOptions.value = []
      grantedTitles.value = []
      knowledgeList.value = []
      adminLogs.value = []
      postTotal.value = 0
      replyTotal.value = 0
      userTotal.value = 0
    }
  )

  return {
    activeTab,
    loading,
    adminAccounts,
    adminPosts,
    adminReplies,
    siteUsers,
    titleOptions,
    grantedTitles,
    knowledgeList,
    adminLogs,
    postTotal,
    replyTotal,
    userTotal,
    adminFilters,
    postFilters,
    replyFilters,
    userFilters,
    knowledgeFilters,
    logFilters,
    adminCreateForm,
    grantTitleForm,
    knowledgeForm,
    isAdmin,
    isSuperAdmin,
    adminRoleLabel,
    roleLabel,
    postStatusLabel,
    knowledgeStatusLabel,
    formatTime,
    targetTypeLabel,
    canBanUser,
    goToPost,
    loadAdminAccounts,
    loadAdminPosts,
    loadAdminReplies,
    loadUserList,
    loadKnowledgeList,
    loadAdminLogs,
    reloadCurrentTab,
    submitCreateAdmin,
    toggleAdminStatus,
    togglePostVisibility,
    confirmDeletePost,
    toggleReplyVisibility,
    confirmDeleteReply,
    openBanPrompt,
    handleUnbanUser,
    submitGrantTitle,
    revokeGrantedTitle,
    submitKnowledge,
    changeKnowledgeStatus
  }
}
