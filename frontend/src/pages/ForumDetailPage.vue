<template>
  <MainLayout>
    <section class="mx-auto w-full max-w-[1320px] px-4 py-8 sm:px-6 lg:px-8">
      <div class="space-y-6">
        <div class="flex items-center gap-3 text-sm text-slate-400">
          <button type="button" class="font-medium text-brand transition hover:text-brand-dark" @click="router.push('/forum')">
            返回论坛
          </button>
          <span>/</span>
          <span>帖子详情</span>
        </div>

        <article v-if="postDetail" class="rounded-[30px] border border-brand/10 bg-white p-6 shadow-soft sm:p-8">
          <div class="flex items-start gap-4">
            <div v-if="postDetail.author?.avatarUrl" class="h-14 w-14 overflow-hidden rounded-full border border-white/80 bg-white shadow-soft">
              <img :src="postDetail.author.avatarUrl" alt="author-avatar" class="h-full w-full object-cover" />
            </div>
            <div v-else class="flex h-14 w-14 items-center justify-center rounded-full bg-[linear-gradient(135deg,#fff_0%,#f4e3e0_100%)] text-lg font-bold text-brand shadow-soft">
              {{ fallbackInitial(postDetail.author?.nickname || postDetail.post.userId) }}
            </div>
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-3">
                <div class="text-[22px] font-bold tracking-tight text-slate-900 sm:text-2xl">
                  {{ postDetail.author?.nickname || '匿名用户' }}
                </div>
                <span v-if="postDetail.author?.title" class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-500">
                  {{ postDetail.author.title }}
                </span>
                <span v-if="postDetail.post.tags" class="rounded-full bg-brand/8 px-3 py-1 text-xs font-semibold text-brand">
                  {{ postDetail.post.tags }}
                </span>
              </div>
              <div class="mt-3 text-sm text-slate-400">
                {{ formatTime(postDetail.post.publishedAt || postDetail.post.createdAt) }}
              </div>
            </div>
          </div>

          <h1 class="mt-8 text-[28px] font-bold leading-tight text-slate-900 sm:text-[32px]">{{ postDetail.post.title }}</h1>
          <div class="prose prose-slate mt-6 max-w-none text-[15px] leading-8" v-html="postDetail.post.content"></div>
          <div v-if="postDetail.imageUrls?.length" class="mt-6 grid gap-4 sm:grid-cols-2">
            <div v-for="(imageUrl, index) in postDetail.imageUrls" :key="`${imageUrl}-${index}`" class="overflow-hidden rounded-[20px] bg-slate-100">
              <img :src="imageUrl" alt="post-image" class="h-64 w-full object-cover" />
            </div>
          </div>
          <div class="mt-8 flex flex-wrap items-center gap-8 border-t border-slate-100 pt-5 text-sm text-slate-500">
            <span>评论 {{ postDetail.post.replyCount || 0 }}</span>
            <button
              type="button"
              class="inline-flex items-center gap-1.5 transition"
              :class="postDetail.liked ? 'font-semibold text-brand' : 'hover:text-brand'"
              @click="toggleDetailPostLike"
            >
              <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 21s-6.716-4.35-9.193-8.015C.943 10.19 2.02 5.75 6.09 4.49c2.12-.657 4.043.08 5.2 1.59 1.157-1.51 3.08-2.247 5.2-1.59 4.07 1.26 5.148 5.7 3.283 8.495C18.716 16.65 12 21 12 21z"/>
              </svg>
              {{ postDetail.liked ? '已赞' : '点赞' }} {{ postDetail.post.likeCount || 0 }}
            </button>
            <span>浏览 {{ postDetail.post.viewCount || 0 }}</span>
          </div>
        </article>

        <section class="rounded-[28px] border border-brand/10 bg-white p-5 shadow-soft sm:p-6">
          <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <div class="text-[11px] font-bold uppercase tracking-[0.3em] text-brand">Reply Box</div>
              <h3 class="mt-2 text-[20px] font-bold text-slate-900">回复主楼</h3>
            </div>
            <div v-if="replyTargetUser" class="rounded-full bg-brand/5 px-4 py-2 text-sm text-brand">
              当前正在楼层内回复：{{ replyTargetUser }}
            </div>
          </div>

          <div v-if="currentUser" class="mt-4 space-y-3">
            <el-input v-model="mainReplyForm.content" type="textarea" :rows="5" resize="none" placeholder="写下你对主楼的回复内容..." />
            <div class="flex justify-end">
              <el-button type="danger" class="!border-brand !bg-brand !font-semibold hover:!bg-brand-dark" :loading="mainReplySubmitting" @click="submitMainReply">
                发表主楼回复
              </el-button>
            </div>
          </div>

          <div v-else class="mt-4 rounded-[18px] bg-slate-50 p-4 text-sm leading-6 text-slate-500">
            登录后才能回复帖子。
            <el-button type="danger" class="!mt-3 !border-brand !bg-brand !font-semibold hover:!bg-brand-dark" @click="openAuth('login')">
              去登录
            </el-button>
          </div>
        </section>

        <section class="rounded-[30px] border border-brand/10 bg-white shadow-soft">
          <div class="border-b border-slate-100 px-6 py-5 sm:px-8">
            <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Reply Floor</div>
                <h2 class="mt-3 text-[30px] font-bold tracking-tight text-slate-900">全部回复</h2>
                <p class="mt-2 text-sm text-slate-500">主楼层连续排列，二级回复收进对应楼层内部，回复关系更接近贴吧的阅读方式。</p>
              </div>
              <el-button class="!border-brand/15 !text-brand" :loading="repliesLoading" @click="loadReplies">刷新回复</el-button>
            </div>
          </div>

          <div class="divide-y divide-slate-100">
            <div v-for="(thread, index) in replyThreads" :key="thread.rootReply.id" class="px-6 py-6 sm:px-8">
              <div class="flex gap-4">
                <div class="shrink-0">
                  <div v-if="thread.rootReply.userAvatarUrl" class="h-12 w-12 overflow-hidden rounded-full border border-white/80 bg-white shadow-soft">
                    <img :src="thread.rootReply.userAvatarUrl" alt="reply-author-avatar" class="h-full w-full object-cover" />
                  </div>
                  <div v-else class="flex h-12 w-12 items-center justify-center rounded-full bg-[linear-gradient(135deg,#fff_0%,#f4e3e0_100%)] text-base font-bold text-brand shadow-soft">
                    {{ (thread.rootReply.userNickname || '友').slice(0, 1) }}
                  </div>
                </div>

                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-3">
                    <div class="text-[20px] font-bold tracking-tight text-slate-900">
                      {{ thread.rootReply.userNickname || `用户${thread.rootReply.userId}` }}
                    </div>
                    <span v-if="thread.rootReply.postAuthor" class="rounded-full bg-brand px-3 py-1 text-xs font-semibold text-white">楼主</span>
                    <span class="rounded-full bg-brand/8 px-3 py-1 text-xs font-semibold text-brand">第{{ index + 1 }}楼</span>
                    <span class="text-sm text-slate-400">{{ formatTime(thread.rootReply.createdAt) }}</span>
                  </div>

                  <div class="mt-4 text-[18px] leading-8 text-slate-900">
                    <span v-if="thread.rootReply.replyToUserNickname" class="font-semibold text-brand">
                      回复 {{ thread.rootReply.replyToUserNickname }}：
                    </span>
                    {{ normalizeContent(thread.rootReply.content) }}
                  </div>

                  <div class="mt-5 flex flex-wrap items-center gap-6 text-sm text-slate-500">
                    <button type="button" class="inline-flex items-center gap-1.5 transition hover:text-brand" @click="prepareReply(thread.rootReply, thread.rootReply)">
                      <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M9 17l-5-5 5-5"/>
                        <path d="M20 18v-2a7 7 0 0 0-7-7H4"/>
                      </svg>
                      回复
                    </button>
                    <button
                      type="button"
                      class="inline-flex items-center gap-1.5 transition"
                      :class="thread.rootReply.liked ? 'font-semibold text-brand' : 'hover:text-brand'"
                      @click="toggleReplyLike(thread.rootReply)"
                    >
                      <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M12 21s-6.716-4.35-9.193-8.015C.943 10.19 2.02 5.75 6.09 4.49c2.12-.657 4.043.08 5.2 1.59 1.157-1.51 3.08-2.247 5.2-1.59 4.07 1.26 5.148 5.7 3.283 8.495C18.716 16.65 12 21 12 21z"/>
                      </svg>
                      {{ thread.rootReply.liked ? '已赞' : '点赞' }} {{ thread.rootReply.likeCount || 0 }}
                    </button>
                  </div>

                  <div v-if="isReplyingTo(thread.rootReply.id)" class="mt-5 rounded-[22px] border border-brand/10 bg-[linear-gradient(180deg,#fff_0%,#fbf7f6_100%)] p-4">
                    <div class="flex items-center justify-between gap-3">
                      <div class="text-sm font-semibold text-brand">{{ replyTargetLabel }}</div>
                      <button type="button" class="text-sm text-slate-400 transition hover:text-brand" @click="resetReplyTarget">取消</button>
                    </div>
                    <div class="mt-2 text-sm text-slate-500">
                      当前正在回复：{{ replyTargetUser || '主楼' }}
                    </div>
                    <div v-if="currentUser" class="mt-3 space-y-3">
                      <el-input v-model="replyForm.content" type="textarea" :rows="4" resize="none" :placeholder="replyPlaceholder" />
                      <div class="flex justify-end">
                        <el-button type="danger" class="!border-brand !bg-brand !font-semibold hover:!bg-brand-dark" :loading="submittingReply" @click="submitReply">
                          发表回复
                        </el-button>
                      </div>
                    </div>
                    <div v-else class="mt-3 rounded-[18px] bg-white p-4 text-sm text-slate-500">
                      登录后才能回复帖子。
                      <el-button type="danger" class="!ml-3 !border-brand !bg-brand !font-semibold hover:!bg-brand-dark" @click="openAuth('login')">
                        去登录
                      </el-button>
                    </div>
                  </div>

                  <div v-if="thread.childReplies?.length" class="mt-6 rounded-[24px] bg-slate-50 px-4 py-4 sm:px-5">
                    <div class="mb-3 text-xs font-bold uppercase tracking-[0.22em] text-slate-400">楼中楼</div>
                    <div class="space-y-3">
                      <div v-for="child in thread.childReplies" :key="child.id" class="rounded-[18px] bg-white px-4 py-4">
                        <div class="flex items-start gap-3">
                          <div v-if="child.userAvatarUrl" class="h-9 w-9 shrink-0 overflow-hidden rounded-full border border-white/80 bg-white shadow-soft">
                            <img :src="child.userAvatarUrl" alt="child-reply-author-avatar" class="h-full w-full object-cover" />
                          </div>
                          <div v-else class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-[linear-gradient(135deg,#fff_0%,#f4e3e0_100%)] text-sm font-bold text-brand shadow-soft">
                            {{ (child.userNickname || '友').slice(0, 1) }}
                          </div>

                          <div class="min-w-0 flex-1">
                            <div class="flex flex-wrap items-center gap-3">
                              <div class="text-[15px] font-semibold text-slate-800">
                                {{ child.userNickname || `用户${child.userId}` }}
                              </div>
                              <span v-if="child.postAuthor" class="rounded-full bg-brand px-3 py-1 text-[11px] font-semibold text-white">楼主</span>
                              <span class="text-xs text-slate-400">{{ formatTime(child.createdAt) }}</span>
                            </div>
                            <div class="mt-2 text-[16px] leading-7 text-slate-900">
                              <span v-if="child.replyToUserNickname" class="font-semibold text-brand">
                                回复 {{ child.replyToUserNickname }}：
                              </span>
                              {{ normalizeContent(child.content) }}
                            </div>
                            <div class="mt-3 flex flex-wrap items-center gap-5 text-sm text-slate-500">
                              <button type="button" class="inline-flex items-center gap-1.5 transition hover:text-brand" @click="prepareReply(thread.rootReply, child)">
                                <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                                  <path d="M9 17l-5-5 5-5"/>
                                  <path d="M20 18v-2a7 7 0 0 0-7-7H4"/>
                                </svg>
                                回复
                              </button>
                              <button
                                type="button"
                                class="inline-flex items-center gap-1.5 transition"
                                :class="child.liked ? 'font-semibold text-brand' : 'hover:text-brand'"
                                @click="toggleReplyLike(child)"
                              >
                                <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                                  <path d="M12 21s-6.716-4.35-9.193-8.015C.943 10.19 2.02 5.75 6.09 4.49c2.12-.657 4.043.08 5.2 1.59 1.157-1.51 3.08-2.247 5.2-1.59 4.07 1.26 5.148 5.7 3.283 8.495C18.716 16.65 12 21 12 21z"/>
                                </svg>
                                {{ child.liked ? '已赞' : '点赞' }} {{ child.likeCount || 0 }}
                              </button>
                            </div>

                            <div v-if="isReplyingTo(child.id)" class="mt-4 rounded-[18px] border border-brand/10 bg-[linear-gradient(180deg,#fff_0%,#fbf7f6_100%)] p-4">
                              <div class="flex items-center justify-between gap-3">
                                <div class="text-sm font-semibold text-brand">{{ replyTargetLabel }}</div>
                                <button type="button" class="text-sm text-slate-400 transition hover:text-brand" @click="resetReplyTarget">取消</button>
                              </div>
                              <div class="mt-2 text-sm text-slate-500">
                                当前正在回复：{{ replyTargetUser }}
                              </div>
                              <div v-if="currentUser" class="mt-3 space-y-3">
                                <el-input v-model="replyForm.content" type="textarea" :rows="4" resize="none" :placeholder="replyPlaceholder" />
                                <div class="flex justify-end">
                                  <el-button type="danger" class="!border-brand !bg-brand !font-semibold hover:!bg-brand-dark" :loading="submittingReply" @click="submitReply">
                                    发表回复
                                  </el-button>
                                </div>
                              </div>
                              <div v-else class="mt-3 rounded-[18px] bg-white p-4 text-sm text-slate-500">
                                登录后才能回复帖子。
                                <el-button type="danger" class="!ml-3 !border-brand !bg-brand !font-semibold hover:!bg-brand-dark" @click="openAuth('login')">
                                  去登录
                                </el-button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="!repliesLoading && !replyThreads.length" class="px-6 py-10 text-sm text-slate-500 sm:px-8">
              还没有人回复这条帖子。
            </div>
          </div>
        </section>

      </div>
    </section>
  </MainLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MainLayout from '../layouts/MainLayout.vue'
import { createReply, fetchPostDetail, fetchPostReplies, likePost, likeReply, unlikePost, unlikeReply } from '../api/forum'
import { useAppShell } from '../stores/appShell'

const route = useRoute()
const router = useRouter()
const { currentUser, ensureShellReady, openAuth } = useAppShell()

const postDetail = ref(null)
const replyThreads = ref([])
const repliesLoading = ref(false)
const submittingReply = ref(false)
const mainReplySubmitting = ref(false)
const replyForm = reactive({ content: '' })
const mainReplyForm = reactive({ content: '' })
const replyTarget = reactive({
  targetReplyId: null,
  parentId: null,
  replyToReplyId: null,
  userNickname: ''
})

const postId = computed(() => route.params.postId)
const replyTargetUser = computed(() => replyTarget.userNickname || '')
const replyTargetLabel = computed(() => (replyTarget.userNickname ? '回复指定楼层' : '回复主楼'))
const replyPlaceholder = computed(() => (replyTarget.userNickname ? `回复 ${replyTarget.userNickname}...` : '写下你的回复内容...'))

function formatTime(value) {
  if (!value) return '刚刚'
  return new Date(value).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function fallbackInitial(value) {
  return String(value || '友').slice(0, 1)
}

function normalizeContent(content) {
  return String(content || '').replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
}

function resetReplyTarget() {
  replyTarget.targetReplyId = null
  replyTarget.parentId = null
  replyTarget.replyToReplyId = null
  replyTarget.userNickname = ''
  replyForm.content = ''
}

function isReplyingTo(replyId) {
  return replyTarget.targetReplyId === replyId
}

function prepareReply(rootReply, targetReply) {
  replyTarget.targetReplyId = targetReply.id
  replyTarget.parentId = rootReply.id
  replyTarget.replyToReplyId = targetReply.id
  replyTarget.userNickname = targetReply.userNickname || `用户${targetReply.userId}`
}

async function loadPostDetailData() {
  try {
    postDetail.value = await fetchPostDetail(postId.value, { incrementView: true })
  } catch (error) {
    ElMessage.error(`帖子详情加载失败: ${error.message}`)
  }
}

async function refreshPostMeta() {
  try {
    postDetail.value = await fetchPostDetail(postId.value, { incrementView: false })
  } catch (error) {
    ElMessage.error(`帖子详情加载失败: ${error.message}`)
  }
}

async function loadReplies() {
  repliesLoading.value = true
  try {
    replyThreads.value = await fetchPostReplies(postId.value)
  } catch (error) {
    replyThreads.value = []
    ElMessage.error(`回复列表加载失败: ${error.message}`)
  } finally {
    repliesLoading.value = false
  }
}

async function submitReply() {
  if (!currentUser.value) {
    openAuth('login')
    return
  }
  submittingReply.value = true
  try {
    const payload = { content: replyForm.content.trim() }
    if (replyTarget.parentId) payload.parentId = replyTarget.parentId
    if (replyTarget.replyToReplyId) payload.replyToReplyId = replyTarget.replyToReplyId
    await createReply(postId.value, payload)
    resetReplyTarget()
    ElMessage.success('回复成功')
    await Promise.all([refreshPostMeta(), loadReplies()])
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submittingReply.value = false
  }
}

async function submitMainReply() {
  if (!currentUser.value) {
    openAuth('login')
    return
  }
  mainReplySubmitting.value = true
  try {
    await createReply(postId.value, { content: mainReplyForm.content.trim() })
    mainReplyForm.content = ''
    ElMessage.success('回复成功')
    await Promise.all([refreshPostMeta(), loadReplies()])
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    mainReplySubmitting.value = false
  }
}

async function toggleDetailPostLike() {
  if (!currentUser.value) {
    openAuth('login')
    return
  }
  try {
    const result = postDetail.value?.liked ? await unlikePost(postId.value) : await likePost(postId.value)
    if (postDetail.value) {
      postDetail.value.liked = result.liked
      postDetail.value.post.likeCount = result.likeCount
    }
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function toggleReplyLike(reply) {
  if (!currentUser.value) {
    openAuth('login')
    return
  }
  try {
    const result = reply.liked ? await unlikeReply(postId.value, reply.id) : await likeReply(postId.value, reply.id)
    reply.liked = result.liked
    reply.likeCount = result.likeCount
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(async () => {
  await ensureShellReady()
  await Promise.all([loadPostDetailData(), loadReplies()])
})

watch(
  () => route.params.postId,
  async (nextValue, oldValue) => {
    if (nextValue && nextValue !== oldValue) {
      resetReplyTarget()
      mainReplyForm.content = ''
      await Promise.all([loadPostDetailData(), loadReplies()])
    }
  }
)
</script>
