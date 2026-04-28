<template>
  <MainLayout>
    <section class="mx-auto w-full max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <div class="grid gap-8 xl:grid-cols-[1.12fr_0.58fr]">
        <div class="space-y-6">
          <div class="flex items-center gap-3 text-sm text-slate-400">
            <button type="button" class="font-medium text-brand transition hover:text-brand-dark" @click="router.push('/forum')">
              返回论坛
            </button>
            <span>/</span>
            <span>帖子详情</span>
          </div>

          <article v-if="postDetail" class="rounded-[32px] border border-brand/10 bg-white p-6 shadow-soft sm:p-8">
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

            <h1 class="mt-8 text-[26px] font-bold leading-tight text-slate-900 sm:text-[30px]">{{ postDetail.post.title }}</h1>
            <div class="prose prose-slate mt-6 max-w-none text-[15px] leading-7" v-html="postDetail.post.content"></div>
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

          <section class="rounded-[32px] border border-brand/10 bg-white p-6 shadow-soft sm:p-8">
            <div class="flex items-center justify-between">
              <div>
                <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Reply Floor</div>
                <h2 class="mt-3 text-3xl font-bold text-slate-900">全部回复</h2>
              </div>
              <el-button class="!border-brand/15 !text-brand" :loading="repliesLoading" @click="loadReplies">刷新回复</el-button>
            </div>

            <div class="mt-8 space-y-8">
              <div v-for="(thread, index) in replyThreads" :key="thread.rootReply.id" class="border-b border-slate-100 pb-8 last:border-b-0 last:pb-0">
                <div class="flex items-start gap-4">
                  <div v-if="thread.rootReply.userAvatarUrl" class="h-12 w-12 overflow-hidden rounded-full border border-white/80 bg-white shadow-soft">
                    <img :src="thread.rootReply.userAvatarUrl" alt="reply-author-avatar" class="h-full w-full object-cover" />
                  </div>
                  <div v-else class="flex h-12 w-12 items-center justify-center rounded-full bg-[linear-gradient(135deg,#fff_0%,#f4e3e0_100%)] text-base font-bold text-brand shadow-soft">
                    {{ (thread.rootReply.userNickname || '友').slice(0, 1) }}
                  </div>
                  <div class="min-w-0 flex-1">
                    <div class="flex flex-wrap items-center gap-3">
                      <div class="text-[18px] font-bold tracking-tight text-slate-800 sm:text-[20px]">
                        {{ thread.rootReply.userNickname || `用户${thread.rootReply.userId}` }}
                      </div>
                      <span
                        v-if="thread.rootReply.postAuthor"
                        class="rounded-full bg-brand px-3 py-1 text-xs font-semibold text-white"
                      >
                        楼主
                      </span>
                      <span class="rounded-full bg-brand/8 px-3 py-1 text-xs font-semibold text-brand">第{{ index + 1 }}楼</span>
                    </div>
                    <div class="mt-2 text-sm text-slate-400">{{ formatTime(thread.rootReply.createdAt) }}</div>
                    <div class="mt-4 text-[18px] leading-8 text-slate-900 sm:text-[20px]">
                      <span v-if="thread.rootReply.replyToUserNickname" class="font-semibold text-brand">
                        回复 {{ thread.rootReply.replyToUserNickname }}：
                      </span>
                      {{ normalizeContent(thread.rootReply.content) }}
                    </div>
                    <div class="mt-5 flex items-center gap-6 text-sm text-slate-500">
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

                    <div v-if="thread.childReplies?.length" class="mt-6 space-y-4 rounded-[24px] bg-slate-50 px-4 py-4">
                      <div v-for="child in thread.childReplies" :key="child.id" class="rounded-[20px] bg-white px-4 py-4">
                        <div class="flex items-start gap-3">
                          <div v-if="child.userAvatarUrl" class="h-10 w-10 shrink-0 overflow-hidden rounded-full border border-white/80 bg-white shadow-soft">
                            <img :src="child.userAvatarUrl" alt="child-reply-author-avatar" class="h-full w-full object-cover" />
                          </div>
                          <div v-else class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[linear-gradient(135deg,#fff_0%,#f4e3e0_100%)] text-sm font-bold text-brand shadow-soft">
                            {{ (child.userNickname || '友').slice(0, 1) }}
                          </div>
                          <div class="min-w-0 flex-1">
                            <div class="flex flex-wrap items-center gap-3">
                              <div class="text-base font-semibold text-slate-800 sm:text-[17px]">
                                {{ child.userNickname || `用户${child.userId}` }}
                              </div>
                              <span
                                v-if="child.postAuthor"
                                class="rounded-full bg-brand px-3 py-1 text-xs font-semibold text-white"
                              >
                                楼主
                              </span>
                              <span class="text-sm text-slate-400">{{ formatTime(child.createdAt) }}</span>
                            </div>
                            <div v-if="child.replyToUserNickname" class="mt-2 text-xs text-slate-400">
                              对 {{ child.replyToUserNickname }} 的回复
                            </div>
                          </div>
                        </div>
                        <div class="mt-3 text-[16px] leading-7 text-slate-900 sm:text-[18px]">
                          <span v-if="child.replyToUserNickname" class="font-semibold text-brand">
                            回复 {{ child.replyToUserNickname }}：
                          </span>
                          {{ normalizeContent(child.content) }}
                        </div>
                        <div class="mt-4 flex items-center gap-6 text-sm text-slate-500">
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
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="!repliesLoading && !replyThreads.length" class="rounded-[24px] bg-slate-50 p-8 text-sm text-slate-500">
                还没有人回复这条帖子。
              </div>
            </div>
          </section>
        </div>

        <aside class="space-y-6">
          <div class="rounded-[28px] border border-brand/10 bg-white p-5 shadow-soft">
            <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Reply Box</div>
            <h3 class="mt-2 text-[24px] font-bold text-slate-800">{{ replyTargetLabel }}</h3>
            <div v-if="replyTargetUser" class="mt-3 rounded-[18px] bg-brand/5 px-4 py-3 text-sm text-brand">
              当前正在回复：{{ replyTargetUser }}
              <button type="button" class="ml-3 font-semibold" @click="resetReplyTarget">取消</button>
            </div>

            <div v-if="currentUser" class="mt-4 space-y-3">
              <el-input v-model="replyForm.content" type="textarea" :rows="6" resize="none" placeholder="写下你的回复内容..." />
              <el-button type="danger" class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark" :loading="submittingReply" @click="submitReply">
                发表回复
              </el-button>
            </div>

            <div v-else class="mt-4 rounded-[20px] bg-slate-50 p-4 text-sm leading-6 text-slate-500">
              登录后才能回复帖子。
              <el-button type="danger" class="!mt-3 !border-brand !bg-brand !font-semibold hover:!bg-brand-dark" @click="openAuth('login')">
                去登录
              </el-button>
            </div>
          </div>
        </aside>
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
const replyForm = reactive({ content: '' })
const replyTarget = reactive({
  parentId: null,
  replyToReplyId: null,
  userNickname: ''
})

const postId = computed(() => route.params.postId)
const replyTargetUser = computed(() => replyTarget.userNickname || '')
const replyTargetLabel = computed(() => (replyTarget.userNickname ? '回复指定楼层' : '回复主楼'))

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
  replyTarget.parentId = null
  replyTarget.replyToReplyId = null
  replyTarget.userNickname = ''
}

function prepareReply(rootReply, targetReply) {
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
    replyForm.content = ''
    resetReplyTarget()
    ElMessage.success('回复成功')
    await Promise.all([refreshPostMeta(), loadReplies()])
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submittingReply.value = false
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
      replyForm.content = ''
      await Promise.all([loadPostDetailData(), loadReplies()])
    }
  }
)
</script>
