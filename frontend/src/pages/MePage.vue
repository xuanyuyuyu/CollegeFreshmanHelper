<template>
  <MainLayout>
    <section class="mx-auto w-full max-w-6xl px-4 py-12 sm:px-6 lg:px-8">
      <div class="rounded-[32px] border border-brand/10 bg-white p-8 shadow-soft">
        <div v-if="currentUser">
          <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <div class="text-xs font-bold uppercase tracking-[0.35em] text-brand">My Center</div>
              <h1 class="mt-4 text-4xl font-bold text-slate-900">我的</h1>
              <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-500">
                这里恢复为更完整的个人中心层级，集中查看我的资料、帖子、回复、获赞与头衔状态。
              </p>
            </div>
            <div class="flex flex-wrap gap-3">
              <el-button class="!border-brand/15 !text-brand" :loading="loading.summary || loading.posts || loading.replies || loading.likes" @click="reloadAll">
                刷新数据
              </el-button>
              <el-button class="!border-brand/15 !text-brand" @click="openAvatarDialog">修改头像</el-button>
              <el-button type="danger" class="!border-brand !bg-brand hover:!bg-brand-dark" @click="openProfileDialog">
                修改个人信息
              </el-button>
            </div>
          </div>

          <div class="mt-8 grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
            <div class="rounded-[30px] bg-[linear-gradient(135deg,#f7f2f1_0%,#fff_100%)] p-6">
              <div class="flex flex-col gap-5 sm:flex-row sm:items-center">
                <div v-if="summary?.avatarUrl" class="h-20 w-20 overflow-hidden rounded-full border border-white/80 bg-white shadow-soft">
                  <img :src="summary.avatarUrl" alt="avatar" class="h-full w-full object-cover" />
                </div>
                <div v-else class="flex h-20 w-20 items-center justify-center rounded-full bg-white text-2xl font-bold text-brand shadow-soft">
                  {{ (summary?.nickname || currentUser.nickname || '友').slice(0, 1) }}
                </div>
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-3">
                    <div class="text-3xl font-bold text-slate-900">{{ summary?.nickname || currentUser.nickname }}</div>
                    <span class="rounded-full bg-brand/10 px-3 py-1 text-xs font-semibold text-brand">
                      {{ summary?.title || '新生伙伴' }}
                    </span>
                  </div>
                  <div class="mt-2 text-sm text-slate-500">账号：{{ summary?.username || currentUser.username }}</div>
                  <div class="mt-1 text-sm text-slate-500">角色：{{ currentUserRoleLabel }}</div>
                  <div class="mt-1 text-sm text-slate-500">
                    {{ summary?.collegeName || '未填写学院' }} / {{ summary?.majorName || '未填写专业' }}
                  </div>
                </div>
              </div>

              <div class="mt-6 rounded-[24px] bg-white/85 p-5 text-sm leading-7 text-slate-500">
                {{ summary?.bio || '暂未填写个人简介。建议补充你的学院、方向和擅长回答的话题，便于后续申请答疑奖励。' }}
              </div>

              <div class="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                <div v-for="card in statCards" :key="card.label" class="rounded-[22px] bg-white px-4 py-4 shadow-soft">
                  <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">{{ card.label }}</div>
                  <div class="mt-3 text-3xl font-bold text-slate-900">{{ card.value }}</div>
                  <div class="mt-2 text-xs leading-5 text-slate-400">{{ card.tip }}</div>
                </div>
              </div>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <div class="rounded-[24px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-5 shadow-soft">
                <div class="text-base font-semibold text-slate-900">奖励资格</div>
                <div class="mt-3 text-sm leading-7 text-slate-500">
                  {{ likesData?.rewardHint || '继续参与讨论并积累高质量回答。' }}
                </div>
                <div class="mt-4">
                  <span
                    class="rounded-full px-3 py-1 text-xs font-semibold"
                    :class="likesData?.rewardEligible ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500'"
                  >
                    {{ likesData?.rewardEligible ? '可申请奖励' : '暂未达标' }}
                  </span>
                </div>
              </div>

              <div class="rounded-[24px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-5 shadow-soft">
                <div class="text-base font-semibold text-slate-900">答疑头衔</div>
                <div class="mt-3 text-2xl font-bold text-slate-900">{{ summary?.title || '新生伙伴' }}</div>
                <div class="mt-2 text-sm leading-7 text-slate-500">
                  根据获赞、精选回答和知识贡献自动评定，后续可继续接管理员审核奖励。
                </div>
              </div>

              <div class="rounded-[24px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-5 shadow-soft">
                <div class="text-base font-semibold text-slate-900">院系与年份</div>
                <div class="mt-3 text-sm leading-7 text-slate-500">
                  入学年份：{{ summary?.admissionYear || '未填写' }}<br />
                  性别：{{ genderLabel(summary?.gender) }}
                </div>
              </div>

              <div class="rounded-[24px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-5 shadow-soft">
                <div class="text-base font-semibold text-slate-900">账号状态</div>
                <div class="mt-3 text-sm leading-7 text-slate-500">
                  {{ summary?.status === 1 ? '正常使用' : '当前受限' }}<br />
                  奖励通过：{{ likesData?.rewardPassedCount || 0 }} 次
                </div>
              </div>
            </div>
          </div>

          <div class="mt-8 rounded-[28px] border border-slate-100 bg-slate-50/70 p-4 sm:p-5">
            <el-tabs v-model="activeTab" class="me-tabs">
              <el-tab-pane label="我的帖子" name="posts">
                <div class="space-y-4">
                  <div v-for="post in myPosts" :key="post.id" class="rounded-[24px] bg-white p-5 shadow-soft">
                    <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                      <div class="min-w-0">
                        <div class="text-lg font-bold text-slate-900">{{ post.title }}</div>
                        <div class="mt-2 text-xs text-slate-400">
                          {{ formatTime(post.publishedAt || post.createdAt) }} · {{ postStatusLabel(post.status) }} · {{ visibilityLabel(post.visibility) }}
                        </div>
                        <div class="mt-3 text-sm leading-7 text-slate-600">{{ post.contentPreview || '暂无正文摘要。' }}</div>
                      </div>
                      <el-button class="!border-brand/15 !text-brand" @click="goToPost(post.id)">查看详情</el-button>
                    </div>
                    <div class="mt-4 flex flex-wrap gap-6 text-sm text-slate-500">
                      <span>标签 {{ post.tags || '未分类' }}</span>
                      <span>评论 {{ post.replyCount || 0 }}</span>
                      <span>点赞 {{ post.likeCount || 0 }}</span>
                    </div>
                  </div>
                  <div v-if="!loading.posts && !myPosts.length" class="rounded-[20px] bg-white p-6 text-sm text-slate-500">
                    你还没有发布帖子。
                  </div>
                </div>
              </el-tab-pane>

              <el-tab-pane label="我的回复" name="replies">
                <div class="space-y-4">
                  <div v-for="reply in myReplies" :key="reply.id" class="rounded-[24px] bg-white p-5 shadow-soft">
                    <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                      <div class="min-w-0">
                        <div class="text-sm font-semibold text-brand">所在帖子：{{ reply.postTitle }}</div>
                        <div class="mt-2 text-xs text-slate-400">
                          {{ formatTime(reply.createdAt) }} · {{ replyStatusLabel(reply) }}
                        </div>
                        <div class="mt-3 text-sm leading-7 text-slate-600">
                          <span v-if="reply.replyToUserNickname" class="font-semibold text-brand">回复 {{ reply.replyToUserNickname }}：</span>
                          {{ reply.contentPreview || '暂无回复内容。' }}
                        </div>
                      </div>
                      <el-button class="!border-brand/15 !text-brand" @click="goToPost(reply.postId)">去原帖</el-button>
                    </div>
                    <div class="mt-4 flex flex-wrap gap-6 text-sm text-slate-500">
                      <span>点赞 {{ reply.likeCount || 0 }}</span>
                      <span>子回复 {{ reply.childCount || 0 }}</span>
                    </div>
                  </div>
                  <div v-if="!loading.replies && !myReplies.length" class="rounded-[20px] bg-white p-6 text-sm text-slate-500">
                    你还没有发表回复。
                  </div>
                </div>
              </el-tab-pane>

              <el-tab-pane label="我的获赞" name="likes">
                <div class="grid gap-4 lg:grid-cols-2">
                  <div class="rounded-[24px] bg-white p-5 shadow-soft">
                    <div class="text-sm font-semibold text-slate-900">帖子获赞</div>
                    <div class="mt-3 text-3xl font-bold text-slate-900">{{ likesData?.postLikeReceivedCount || 0 }}</div>
                  </div>
                  <div class="rounded-[24px] bg-white p-5 shadow-soft">
                    <div class="text-sm font-semibold text-slate-900">回复获赞</div>
                    <div class="mt-3 text-3xl font-bold text-slate-900">{{ likesData?.replyLikeReceivedCount || 0 }}</div>
                  </div>
                  <div class="rounded-[24px] bg-white p-5 shadow-soft">
                    <div class="text-sm font-semibold text-slate-900">精选回答</div>
                    <div class="mt-3 text-3xl font-bold text-slate-900">{{ likesData?.featuredAnswerCount || 0 }}</div>
                  </div>
                  <div class="rounded-[24px] bg-white p-5 shadow-soft">
                    <div class="text-sm font-semibold text-slate-900">知识贡献</div>
                    <div class="mt-3 text-3xl font-bold text-slate-900">{{ likesData?.knowledgeContributionCount || 0 }}</div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>

        <div v-else class="text-center">
          <div class="text-xs font-bold uppercase tracking-[0.35em] text-brand">My Center</div>
          <h1 class="mt-4 text-4xl font-bold text-slate-900">我的</h1>
          <p class="mx-auto mt-5 max-w-2xl text-base leading-8 text-slate-600">
            登录后可查看我的帖子、我的回复、获赞统计、答疑头衔和资料编辑入口。
          </p>
          <el-button type="danger" class="!mt-8 !border-brand !bg-brand hover:!bg-brand-dark" @click="openAuth('login')">
            立即登录
          </el-button>
        </div>
      </div>
    </section>

    <el-dialog v-model="profileDialogVisible" width="520px" title="修改个人信息">
      <div class="space-y-4">
        <el-input v-model="profileForm.nickname" maxlength="32" placeholder="昵称" />
        <div class="grid gap-4 sm:grid-cols-2">
          <el-select v-model="profileForm.gender" placeholder="性别">
            <el-option label="未填写" :value="0" />
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
          <el-input-number v-model="profileForm.admissionYear" :min="2000" :max="2100" class="!w-full" placeholder="入学年份" />
        </div>
        <div class="grid gap-4 sm:grid-cols-2">
          <el-input v-model="profileForm.collegeName" maxlength="64" placeholder="学院名称" />
          <el-input v-model="profileForm.majorName" maxlength="64" placeholder="专业名称" />
        </div>
        <el-input v-model="profileForm.bio" type="textarea" :rows="5" maxlength="255" show-word-limit resize="none" placeholder="个人简介" />
        <el-button
          type="danger"
          class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark"
          :loading="profileSubmitting"
          @click="submitProfile"
        >
          保存修改
        </el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="avatarDialogVisible" width="520px" title="修改头像">
      <div class="space-y-4">
        <div v-if="avatarForm.avatarUrl" class="mx-auto h-24 w-24 overflow-hidden rounded-full border border-slate-100 shadow-soft">
          <img :src="avatarForm.avatarUrl" alt="avatar-preview" class="h-full w-full object-cover" />
        </div>
        <el-input v-model="avatarForm.avatarUrl" placeholder="请输入头像图片 URL" />
        <p class="text-sm leading-6 text-slate-500">
          当前版本先走头像地址更新，后续接入 OSS 直传后可替换为真正的上传流程。
        </p>
        <el-button
          type="danger"
          class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark"
          :loading="avatarSubmitting"
          @click="submitAvatar"
        >
          更新头像
        </el-button>
      </div>
    </el-dialog>
  </MainLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MainLayout from '../layouts/MainLayout.vue'
import { fetchMyLikes, fetchMyPosts, fetchMyReplies, fetchMySummary, updateMyAvatar, updateMyProfile } from '../api/me'
import { useAppShell } from '../stores/appShell'

const router = useRouter()
const { currentUser, currentUserRoleLabel, ensureShellReady, openAuth, refreshCurrentUser } = useAppShell()
const activeTab = ref('posts')
const summary = ref(null)
const likesData = ref(null)
const myPosts = ref([])
const myReplies = ref([])
const profileDialogVisible = ref(false)
const avatarDialogVisible = ref(false)
const loading = reactive({
  summary: false,
  posts: false,
  replies: false,
  likes: false
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
  { label: '积分', value: summary.value?.points || 0, tip: '社区积分与后台奖励直接挂钩' },
  { label: '帖子', value: summary.value?.postCount || 0, tip: '我发出的主贴数量' },
  { label: '回复', value: summary.value?.replyCount || 0, tip: '我参与答疑的回复数量' },
  { label: '获赞', value: summary.value?.totalLikeReceivedCount || 0, tip: '帖子与回复累计获赞' }
])

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

function goToPost(postId) {
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
    const page = await fetchMyPosts({ pageNum: 1, pageSize: 8 })
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
    const page = await fetchMyReplies({ pageNum: 1, pageSize: 8 })
    myReplies.value = page.records || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.replies = false
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

async function reloadAll() {
  if (!currentUser.value) return
  await Promise.all([loadSummary(), loadPosts(), loadReplies(), loadLikes()])
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

onMounted(async () => {
  await ensureShellReady()
  if (currentUser.value) {
    avatarForm.avatarUrl = currentUser.value.avatarUrl || ''
    await reloadAll()
  }
})

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
      myPosts.value = []
      myReplies.value = []
    }
  }
)
</script>

<style scoped>
:deep(.me-tabs .el-tabs__item.is-active) {
  color: #8b261e;
}

:deep(.me-tabs .el-tabs__active-bar) {
  background-color: #8b261e;
}
</style>
