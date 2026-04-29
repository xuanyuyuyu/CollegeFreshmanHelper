import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deletePost as deleteOwnPost, deleteReply as deleteOwnReply } from '../api/forum'
import { fetchMyLikeDetails, fetchMyLikedItems, fetchMyLikes, fetchMyPosts, fetchMyReplies, fetchMySummary, updateMyAvatar, updateMyProfile } from '../api/me'

const LIST_FETCH_SIZE = 100

export function useMyCenter(appShell) {
  const router = useRouter()
  const { currentUser, refreshCurrentUser } = appShell

  const activeTab = ref('posts')
  const summary = ref(null)
  const likesData = ref(null)
  const likeDetails = ref([])
  const myLikedItems = ref([])
  const myPosts = ref([])
  const myReplies = ref([])
  const profileDialogVisible = ref(false)
  const avatarDialogVisible = ref(false)
  const loading = reactive({
    summary: false,
    posts: false,
    replies: false,
    likes: false,
    likedItems: false,
    likeDetails: false
  })
  const profileSubmitting = ref(false)
  const avatarSubmitting = ref(false)
  const profileForm = reactive({
    nickname: '',
    gender: 0,
    admissionYear: null,
    collegeName: '',
    majorName: '',
    bio: ''
  })
  const avatarForm = reactive({
    avatarUrl: ''
  })

  const statCards = computed(() => [
    { label: '积分', value: summary.value?.points || 0, tip: '积分可反映你的社区活跃度与贡献度' },
    { label: '帖子', value: summary.value?.postCount || 0, tip: '我发出的主贴数量' },
    { label: '回复', value: summary.value?.replyCount || 0, tip: '我参与答疑的回复数量' },
    { label: '获赞', value: summary.value?.totalLikeReceivedCount || 0, tip: '当前数据库中的帖子与回复累计获赞' }
  ])
  const titleRules = computed(() => summary.value?.titleRules || [])

  function genderLabel(value) {
    if (value === 1) return '男'
    if (value === 2) return '女'
    return '未填写'
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

  function postStatusLabel(status) {
    if (status === 1) return '已发布'
    if (status === 0) return '审核中'
    return '未知状态'
  }

  function visibilityLabel(visibility) {
    return visibility === 1 ? '可见' : '隐藏'
  }

  function replyStatusLabel(reply) {
    return `${reply.status === 1 ? '正常' : '受限'} / ${reply.visibility === 1 ? '可见' : '隐藏'}`
  }

  function itemStatusLabel(status, visibility) {
    return `${status === 1 ? '正常' : '受限'} / ${visibility === 1 ? '可见' : '隐藏'}`
  }

  function goToPost(postId) {
    if (!postId) return
    router.push(`/forum/${postId}`)
  }

  function openProfileDialog() {
    profileForm.nickname = summary.value?.nickname || currentUser.value?.nickname || ''
    profileForm.gender = summary.value?.gender ?? 0
    profileForm.admissionYear = summary.value?.admissionYear ?? null
    profileForm.collegeName = summary.value?.collegeName || ''
    profileForm.majorName = summary.value?.majorName || ''
    profileForm.bio = summary.value?.bio || ''
    profileDialogVisible.value = true
  }

  function openAvatarDialog() {
    avatarForm.avatarUrl = summary.value?.avatarUrl || currentUser.value?.avatarUrl || ''
    avatarDialogVisible.value = true
  }

  async function loadSummary() {
    loading.summary = true
    try {
      summary.value = await fetchMySummary()
      avatarForm.avatarUrl = summary.value?.avatarUrl || ''
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      loading.summary = false
    }
  }

  async function loadPosts() {
    loading.posts = true
    try {
      const page = await fetchMyPosts({ pageNum: 1, pageSize: LIST_FETCH_SIZE })
      myPosts.value = page.records || []
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      loading.posts = false
    }
  }

  async function loadReplies() {
    loading.replies = true
    try {
      const page = await fetchMyReplies({ pageNum: 1, pageSize: LIST_FETCH_SIZE })
      myReplies.value = page.records || []
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      loading.replies = false
    }
  }

  async function loadLikedItems() {
    loading.likedItems = true
    try {
      const page = await fetchMyLikedItems({ pageNum: 1, pageSize: LIST_FETCH_SIZE })
      myLikedItems.value = page.records || []
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      loading.likedItems = false
    }
  }

  async function loadLikes() {
    loading.likes = true
    try {
      likesData.value = await fetchMyLikes()
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      loading.likes = false
    }
  }

  async function loadLikeDetails() {
    loading.likeDetails = true
    try {
      const page = await fetchMyLikeDetails({ pageNum: 1, pageSize: LIST_FETCH_SIZE })
      likeDetails.value = page.records || []
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      loading.likeDetails = false
    }
  }

  async function reloadAll() {
    if (!currentUser.value) return
    await Promise.all([loadSummary(), loadPosts(), loadReplies(), loadLikedItems(), loadLikes(), loadLikeDetails()])
  }

  async function confirmDeleteMyPost(post) {
    try {
      await ElMessageBox.confirm('确认删除这条帖子？删除后帖子及其全部回复都会被移除。', '删除帖子', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await deleteOwnPost(post.id)
      ElMessage.success('帖子已删除')
      await reloadAll()
    } catch (error) {
      if (error === 'cancel' || error === 'close') return
      ElMessage.error(error.message || '删除失败')
    }
  }

  async function confirmDeleteMyReply(reply) {
    try {
      const isRootReply = !reply.parentId
      await ElMessageBox.confirm(
        isRootReply ? '确认删除这条主楼回复？该楼层下的所有子回复也会一起删除。' : '确认删除这条回复？',
        '删除回复',
        {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      await deleteOwnReply(reply.postId, reply.id)
      ElMessage.success('回复已删除')
      await reloadAll()
    } catch (error) {
      if (error === 'cancel' || error === 'close') return
      ElMessage.error(error.message || '删除失败')
    }
  }

  async function submitProfile() {
    profileSubmitting.value = true
    try {
      summary.value = await updateMyProfile({
        nickname: profileForm.nickname.trim(),
        gender: profileForm.gender,
        admissionYear: profileForm.admissionYear,
        collegeName: profileForm.collegeName,
        majorName: profileForm.majorName,
        bio: profileForm.bio
      })
      await refreshCurrentUser()
      profileDialogVisible.value = false
      ElMessage.success('个人资料已更新')
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      profileSubmitting.value = false
    }
  }

  async function submitAvatar() {
    avatarSubmitting.value = true
    try {
      summary.value = await updateMyAvatar({ avatarUrl: avatarForm.avatarUrl.trim() })
      await refreshCurrentUser()
      avatarDialogVisible.value = false
      ElMessage.success('头像已更新')
    } catch (error) {
      ElMessage.error(error.message)
    } finally {
      avatarSubmitting.value = false
    }
  }

  watch(
    () => currentUser.value?.id,
    async (nextId, previousId) => {
      if (nextId && nextId !== previousId) {
        await reloadAll()
        return
      }
      if (!nextId) {
        summary.value = null
        likesData.value = null
        likeDetails.value = []
        myLikedItems.value = []
        myPosts.value = []
        myReplies.value = []
      }
    }
  )

  return {
    activeTab,
    summary,
    likesData,
    likeDetails,
    myLikedItems,
    myPosts,
    myReplies,
    profileDialogVisible,
    avatarDialogVisible,
    loading,
    profileSubmitting,
    avatarSubmitting,
    profileForm,
    avatarForm,
    statCards,
    titleRules,
    genderLabel,
    formatTime,
    postStatusLabel,
    visibilityLabel,
    replyStatusLabel,
    itemStatusLabel,
    goToPost,
    openProfileDialog,
    openAvatarDialog,
    loadLikeDetails,
    reloadAll,
    confirmDeleteMyPost,
    confirmDeleteMyReply,
    submitProfile,
    submitAvatar
  }
}
