<template>
  <MainLayout>
    <section class="mx-auto box-border h-[calc(100vh-88px)] w-full max-w-[1680px] overflow-hidden px-4 py-6 sm:px-6 lg:px-8">
      <div class="grid h-full min-h-0 gap-6 xl:grid-cols-[1.45fr_0.55fr]">
        <div class="min-h-0">
          <div class="flex h-full min-h-0 flex-col rounded-[32px] border border-brand/10 bg-white p-6 shadow-soft sm:p-8">
            <div class="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
              <div>
                <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Forum Plaza</div>
                <h1 class="mt-3 text-3xl font-bold text-slate-800 sm:text-[40px]">论坛交流</h1>
                <p class="mt-3 max-w-4xl text-sm leading-7 text-slate-500">
                  当前这套前端按现有后端接口重建，优先保证帖子列表、帖子详情和双层回复结构可以直接联调。
                </p>
              </div>

              <div class="flex flex-wrap gap-3">
                <el-select v-model="sortType" class="!w-[140px]" @change="loadPosts">
                  <el-option label="最新发布" value="latest" />
                  <el-option label="热门优先" value="hottest" />
                </el-select>
                <el-button class="!border-brand/15 !text-brand" :loading="loadingPosts" @click="loadPosts">
                  刷新帖子
                </el-button>
                <el-button type="danger" class="!border-brand !bg-brand hover:!bg-brand-dark" @click="currentUser ? scrollToComposer() : openAuth('login')">
                  {{ currentUser ? '发布新帖' : '登录后发帖' }}
                </el-button>
              </div>
            </div>

            <div class="mt-6 min-h-0 flex-1 space-y-4 overflow-y-auto pr-2">
              <article
                v-for="post in posts"
                :key="post.id"
                class="overflow-hidden rounded-[26px] border border-slate-100 bg-[linear-gradient(180deg,#fbfbfc_0%,#f8fafc_100%)] transition hover:border-brand/15 hover:bg-white hover:shadow-soft"
              >
                <button type="button" class="block w-full text-left" @click="goToPost(post.id)">
                  <div class="p-5 sm:p-6">
                    <div class="flex items-start gap-4">
                      <div v-if="post.author?.avatarUrl" class="h-12 w-12 shrink-0 overflow-hidden rounded-full border border-white/80 bg-white shadow-soft">
                        <img :src="post.author.avatarUrl" alt="author-avatar" class="h-full w-full object-cover" />
                      </div>
                      <div v-else class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-[linear-gradient(135deg,#fff_0%,#f4e3e0_100%)] text-lg font-bold text-brand shadow-soft">
                        {{ fallbackInitial(post.author?.nickname || post.author?.userId) }}
                      </div>

                      <div class="min-w-0 flex-1">
                        <div class="flex flex-wrap items-center gap-3">
                          <div class="text-lg font-bold tracking-tight text-slate-800 sm:text-xl">
                            {{ post.author?.nickname || '匿名用户' }}
                          </div>
                          <span v-if="post.author?.title" class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-500">
                            {{ post.author.title }}
                          </span>
                          <span v-if="post.tags" class="rounded-full bg-brand/8 px-3 py-1 text-xs font-semibold text-brand">
                            {{ post.tags }}
                          </span>
                        </div>
                        <div class="mt-2 text-xs text-slate-400 sm:text-sm">
                          发布于 {{ formatDateTime(post.publishedAt || post.createdAt) }}
                        </div>

                        <h2 class="mt-4 text-2xl font-bold leading-tight text-slate-900 sm:text-[26px]">
                          {{ post.title }}
                        </h2>
                        <p class="mt-3 line-clamp-3 text-sm leading-6 text-slate-600">
                          {{ post.contentPreview || '这条帖子还没有正文摘要。' }}
                        </p>
                      </div>
                    </div>
                  </div>
                </button>

                <div class="flex items-center gap-8 border-t border-slate-200/80 bg-white px-5 py-3 text-sm text-slate-500 sm:px-6">
                  <span>评论 {{ post.replyCount || 0 }}</span>
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
                  <span>浏览 {{ post.viewCount || 0 }}</span>
                </div>
              </article>

              <div v-if="!loadingPosts && !posts.length" class="rounded-[24px] bg-slate-50 p-8 text-sm text-slate-500">
                当前还没有可展示的帖子。
              </div>
            </div>
          </div>
        </div>

        <aside ref="composerRef" class="sticky top-[108px] h-fit self-start pr-1">
          <div class="space-y-4">
            <div class="rounded-[28px] border border-brand/10 bg-white p-5 shadow-soft">
              <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Post Tips</div>
              <h3 class="mt-2 text-[24px] font-bold text-slate-800">发帖建议</h3>
              <div class="mt-3 space-y-2 text-sm leading-7 text-slate-500">
                <p>标题尽量直接点明问题，例如“宿舍床垫需要自己买吗”。</p>
                <p>正文里先写背景，再写你的具体困惑，老生更容易快速给出有效回复。</p>
                <p>当前标签先支持单值输入，后续再和更完整的分类体系对齐。</p>
              </div>
            </div>

            <div class="rounded-[28px] border border-brand/10 bg-white p-5 shadow-soft">
              <div class="flex items-center justify-between">
                <div>
                  <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Create Post</div>
                  <h3 class="mt-2 text-[24px] font-bold text-slate-800">发布新帖</h3>
                </div>
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

              <div v-else class="mt-4 rounded-[20px] bg-slate-50 p-4 text-sm leading-6 text-slate-500">
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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MainLayout from '../layouts/MainLayout.vue'
import { createPost, fetchPostPage, likePost, unlikePost } from '../api/forum'
import { useAppShell } from '../stores/appShell'

const router = useRouter()
const composerRef = ref(null)
const { currentUser, ensureShellReady, openAuth } = useAppShell()

const posts = ref([])
const loadingPosts = ref(false)
const submittingPost = ref(false)
const sortType = ref('latest')
const tagOptions = ['宿舍', '学习', '食堂', '军训', '其他']
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

async function loadPosts() {
  loadingPosts.value = true
  try {
    const page = await fetchPostPage({ pageNum: 1, pageSize: 12, sortType: sortType.value })
    posts.value = page.records || []
  } catch (error) {
    ElMessage.error(`帖子列表加载失败: ${error.message}`)
  } finally {
    loadingPosts.value = false
  }
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
    await loadPosts()
    goToPost(post.id)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submittingPost.value = false
  }
}

onMounted(async () => {
  await Promise.all([ensureShellReady(), loadPosts()])
})
</script>
