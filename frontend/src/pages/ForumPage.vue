<template>
  <MainLayout>
    <section class="mx-auto w-full max-w-[1760px] px-4 py-6 sm:px-6 lg:px-8">
      <div class="grid gap-6 xl:grid-cols-[minmax(0,1.75fr)_320px]">
        <div class="min-w-0">
          <div class="overflow-hidden rounded-[30px] border border-brand/10 bg-white shadow-soft">
            <div class="border-b border-slate-100 bg-[linear-gradient(180deg,#fff_0%,#fbf7f6_100%)] px-5 py-5 sm:px-7">
              <div class="flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
                <div>
                  <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Forum Plaza</div>
                  <h1 class="mt-3 text-[30px] font-bold tracking-tight text-slate-900 sm:text-[36px]">论坛交流</h1>
                  <p class="mt-3 max-w-4xl text-sm leading-7 text-slate-500">
                    宿舍、学习、食堂、军训与校园生活问题集中在这里讨论。现在采用更接近贴吧的长列表布局，让浏览帖子成为页面核心。
                  </p>
                </div>

                <div class="flex flex-wrap gap-3">
                  <el-select v-model="sortType" class="!w-[144px]" @change="resetPostFeed">
                    <el-option label="最新发布" value="latest" />
                    <el-option label="热门优先" value="hottest" />
                  </el-select>
                  <el-button class="!border-brand/15 !text-brand" :loading="loadingPosts" @click="resetPostFeed">
                    刷新帖子
                  </el-button>
                  <el-button type="danger" class="!border-brand !bg-brand hover:!bg-brand-dark" @click="currentUser ? scrollToComposer() : openAuth('login')">
                    {{ currentUser ? '发布新帖' : '登录后发帖' }}
                  </el-button>
                </div>
              </div>
            </div>

            <div class="flex flex-wrap items-center gap-3 border-b border-slate-100 bg-slate-50/80 px-5 py-4 sm:px-7">
              <button
                v-for="tab in categoryTabs"
                :key="tab.value"
                type="button"
                class="rounded-full px-4 py-2 text-sm font-semibold transition"
                :class="selectedCategory === tab.value ? 'bg-brand text-white shadow-soft' : 'bg-white text-slate-500 hover:text-brand'"
                @click="selectedCategory = tab.value"
              >
                {{ tab.label }}
              </button>
            </div>

            <div class="divide-y divide-slate-100">
              <article
                v-for="post in posts"
                :key="post.id"
                class="group transition hover:bg-[linear-gradient(180deg,#fff_0%,#fcfbfb_100%)]"
              >
                <button type="button" class="block w-full text-left" @click="goToPost(post.id)">
                  <div class="flex gap-4 px-5 py-5 sm:px-7">
                    <div class="shrink-0">
                      <div v-if="post.author?.avatarUrl" class="h-12 w-12 overflow-hidden rounded-full border border-white/80 bg-white shadow-soft">
                        <img :src="post.author.avatarUrl" alt="author-avatar" class="h-full w-full object-cover" />
                      </div>
                      <div v-else class="flex h-12 w-12 items-center justify-center rounded-full bg-[linear-gradient(135deg,#fff_0%,#f4e3e0_100%)] text-base font-bold text-brand shadow-soft">
                        {{ fallbackInitial(post.author?.nickname || post.author?.userId) }}
                      </div>
                    </div>

                    <div class="min-w-0 flex-1">
                      <div class="flex flex-wrap items-center gap-x-3 gap-y-2">
                        <div class="text-[15px] font-semibold text-slate-700">
                          {{ post.author?.nickname || '匿名用户' }}
                        </div>
                        <span
                          v-if="post.author?.title"
                          class="rounded-full px-3 py-1 text-[11px] font-semibold"
                          :class="titleBadgeClass(post.author.title)"
                        >
                          {{ post.author.title }}
                        </span>
                        <span v-if="post.tags" class="rounded-full bg-brand/8 px-3 py-1 text-[11px] font-semibold text-brand">
                          {{ post.tags }}
                        </span>
                        <span class="text-xs text-slate-400">
                          {{ formatDateTime(post.publishedAt || post.createdAt) }}
                        </span>
                      </div>

                      <div class="mt-3 flex items-start gap-4">
                        <div class="min-w-0 flex-1">
                          <h2 class="line-clamp-1 text-[21px] font-bold tracking-tight text-slate-900 transition group-hover:text-brand sm:text-[23px]">
                            {{ post.title }}
                          </h2>
                          <p class="mt-3 line-clamp-3 text-[14px] leading-7 text-slate-600">
                            {{ post.contentPreview || '这条帖子还没有正文摘要。' }}
                          </p>
                        </div>

                        <div
                          v-if="post.firstImageUrl"
                          class="hidden h-[96px] w-[128px] shrink-0 overflow-hidden rounded-[18px] bg-slate-100 lg:block"
                        >
                          <img :src="post.firstImageUrl" alt="post-cover" class="h-full w-full object-cover transition duration-300 group-hover:scale-[1.04]" />
                        </div>
                      </div>
                    </div>
                  </div>
                </button>

                <div class="flex flex-wrap items-center gap-x-8 gap-y-3 px-5 pb-5 text-[13px] text-slate-500 sm:px-7">
                  <button type="button" class="inline-flex items-center gap-1.5 transition hover:text-brand" @click.stop="goToPost(post.id)">
                    <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                    </svg>
                    回复 {{ post.replyCount || 0 }}
                  </button>
                  <button
                    type="button"
                    class="inline-flex items-center gap-1.5 transition"
                    :class="post.liked ? 'font-semibold text-brand' : 'hover:text-brand'"
                    @click.stop="togglePostLike(post)"
                  >
                    <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M12 21s-6.716-4.35-9.193-8.015C.943 10.19 2.02 5.75 6.09 4.49c2.12-.657 4.043.08 5.2 1.59 1.157-1.51 3.08-2.247 5.2-1.59 4.07 1.26 5.148 5.7 3.283 8.495C18.716 16.65 12 21 12 21z"/>
                    </svg>
                    {{ post.liked ? '已赞' : '点赞' }} {{ post.likeCount || 0 }}
                  </button>
                  <span class="inline-flex items-center gap-1.5">
                    <svg viewBox="0 0 24 24" class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                    浏览 {{ post.viewCount || 0 }}
                  </span>
                </div>
              </article>

              <div v-if="!loadingPosts && !posts.length" class="px-5 py-10 text-sm text-slate-500 sm:px-7">
                当前分类下还没有可展示的帖子。
              </div>

              <div v-if="posts.length" class="px-5 py-6 text-center text-sm text-slate-500 sm:px-7">
                <div ref="loadMoreTrigger" class="h-1 w-full"></div>
                <div v-if="loadingMore" class="py-2">正在加载更多帖子...</div>
                <button
                  v-else-if="loadFailed"
                  type="button"
                  class="rounded-full border border-brand/15 px-4 py-2 text-brand transition hover:bg-brand/5"
                  @click="loadMorePosts"
                >
                  加载失败，点击重试
                </button>
                <div v-else-if="!hasMore && initialLoaded" class="py-2">没有更多帖子了</div>
              </div>
            </div>
          </div>
        </div>

        <aside ref="composerRef" class="xl:sticky xl:top-[104px] xl:self-start">
          <div class="space-y-4">
            <div class="rounded-[26px] border border-brand/10 bg-white p-5 shadow-soft">
              <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Post Tips</div>
              <h3 class="mt-2 text-[22px] font-bold text-slate-900">发帖建议</h3>
              <div class="mt-3 space-y-2 text-[13px] leading-6 text-slate-500">
                <p>标题尽量点明场景，例如“军训鞋垫需要自备吗”。</p>
                <p>正文先写背景，再写困惑，老生更容易给到有效建议。</p>
                <p>如果问题只属于单一主题，优先选择一个明确标签。</p>
              </div>
            </div>

            <div class="rounded-[26px] border border-brand/10 bg-white p-5 shadow-soft">
              <div>
                <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Create Post</div>
                <h3 class="mt-2 text-[22px] font-bold text-slate-900">发布新帖</h3>
              </div>

              <div v-if="currentUser" class="mt-4 space-y-3">
                <el-input v-model="postForm.title" placeholder="帖子标题" maxlength="120" />
                <el-select v-model="postForm.tags" placeholder="选择帖子标签" class="w-full">
                  <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
                </el-select>
                <el-input
                  v-model="postForm.content"
                  type="textarea"
                  :rows="5"
                  resize="none"
                  placeholder="把你的问题或经验写清楚，进入详情页后大家会围绕主楼继续回复。"
                />
                <el-button type="danger" class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark" :loading="submittingPost" @click="submitPost">
                  立即发布
                </el-button>
              </div>

              <div v-else class="mt-4 rounded-[18px] bg-slate-50 p-4 text-sm leading-6 text-slate-500">
                当前未登录，请先登录后发帖。
                <el-button type="danger" class="!mt-3 !border-brand !bg-brand !font-semibold hover:!bg-brand-dark" @click="openAuth('login')">
                  去登录
                </el-button>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </section>
  </MainLayout>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MainLayout from '../layouts/MainLayout.vue'
import { createPost, fetchPostPage, likePost, unlikePost } from '../api/forum'
import { useAppShell } from '../stores/appShell'
import { titleBadgeClass } from '../utils/titleBadge'

const router = useRouter()
const composerRef = ref(null)
const loadMoreTrigger = ref(null)
const { currentUser, ensureShellReady, openAuth } = useAppShell()

const posts = ref([])
const loadingPosts = ref(false)
const loadingMore = ref(false)
const submittingPost = ref(false)
const sortType = ref('latest')
const selectedCategory = ref('all')
const pageNum = ref(1)
const pageSize = ref(16)
const hasMore = ref(true)
const initialLoaded = ref(false)
const loadFailed = ref(false)
let loadMoreObserver = null
const tagOptions = ['宿舍', '学习', '食堂', '军训', '其他']
const categoryTabs = [
  { label: '全部帖子', value: 'all' },
  { label: '宿舍', value: '宿舍' },
  { label: '学习', value: '学习' },
  { label: '食堂', value: '食堂' },
  { label: '军训', value: '军训' },
  { label: '其他', value: '其他' }
]
const postForm = reactive({
  title: '',
  tags: '',
  content: ''
})

function fallbackInitial(value) {
  return String(value || '友').slice(0, 1)
}

function formatDateTime(value) {
  if (!value) return '刚刚'
  return new Date(value).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function scrollToComposer() {
  composerRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function goToPost(postId) {
  router.push(`/forum/${postId}`)
}

function currentTagParam() {
  return selectedCategory.value === 'all' ? undefined : selectedCategory.value
}

async function loadPosts({ reset = false } = {}) {
  if (reset) {
    pageNum.value = 1
    hasMore.value = true
    loadFailed.value = false
    initialLoaded.value = false
  }
  if (!reset && (!hasMore.value || loadingMore.value || loadingPosts.value)) {
    return
  }
  if (reset) {
    loadingPosts.value = true
  } else {
    loadingMore.value = true
  }
  try {
    const page = await fetchPostPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      sortType: sortType.value,
      tag: currentTagParam()
    })
    const records = page.records || []
    if (reset) {
      posts.value = records
    } else {
      posts.value = [...posts.value, ...records]
    }
    const loadedCount = posts.value.length
    const total = Number(page.total || 0)
    hasMore.value = loadedCount < total && records.length > 0
    loadFailed.value = false
    initialLoaded.value = true
  } catch (error) {
    loadFailed.value = true
    ElMessage.error(`帖子列表加载失败: ${error.message}`)
  } finally {
    if (reset) {
      loadingPosts.value = false
    } else {
      loadingMore.value = false
    }
  }
}

async function loadMorePosts() {
  if (!hasMore.value || loadingMore.value || loadingPosts.value) return
  pageNum.value += 1
  await loadPosts()
}

async function resetPostFeed() {
  posts.value = []
  await loadPosts({ reset: true })
  await nextTick()
  observeLoadMoreTrigger()
}

function observeLoadMoreTrigger() {
  if (!loadMoreObserver || !loadMoreTrigger.value) return
  loadMoreObserver.disconnect()
  loadMoreObserver.observe(loadMoreTrigger.value)
}

function setupLoadMoreObserver() {
  if (typeof IntersectionObserver === 'undefined') {
    return
  }
  loadMoreObserver = new IntersectionObserver(
    (entries) => {
      if (entries.some((entry) => entry.isIntersecting)) {
        loadMorePosts()
      }
    },
    { rootMargin: '240px 0px' }
  )
  observeLoadMoreTrigger()
}

async function togglePostLike(post) {
  if (!currentUser.value) {
    openAuth('login')
    return
  }
  try {
    const result = post.liked ? await unlikePost(post.id) : await likePost(post.id)
    post.liked = result.liked
    post.likeCount = result.likeCount
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function submitPost() {
  if (!currentUser.value) {
    openAuth('login')
    return
  }
  submittingPost.value = true
  try {
    const post = await createPost({
      title: postForm.title.trim(),
      tags: postForm.tags,
      content: `<p>${postForm.content.trim()}</p>`
    })
    postForm.title = ''
    postForm.tags = ''
    postForm.content = ''
    ElMessage.success(`帖子已发布：${post.title}`)
    selectedCategory.value = 'all'
    sortType.value = 'latest'
    await resetPostFeed()
    goToPost(post.id)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submittingPost.value = false
  }
}

onMounted(async () => {
  await ensureShellReady()
  await resetPostFeed()
  setupLoadMoreObserver()
})

onBeforeUnmount(() => {
  loadMoreObserver?.disconnect()
})

watch(sortType, async (nextValue, oldValue) => {
  if (nextValue !== oldValue) {
    await resetPostFeed()
  }
})

watch(selectedCategory, async (nextValue, oldValue) => {
  if (nextValue !== oldValue) {
    await resetPostFeed()
  }
})
</script>
