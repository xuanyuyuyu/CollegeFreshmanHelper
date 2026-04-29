<template>
  <div class="space-y-4">
    <div class="grid gap-4 md:grid-cols-3">
      <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
        <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">筛选结果总数</div>
        <div class="mt-3 text-3xl font-bold text-slate-900">{{ total }}</div>
        <div class="mt-2 text-sm text-slate-500">当前筛选条件下命中的帖子总数。</div>
      </div>
      <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
        <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">删除动作</div>
        <div class="mt-3 text-lg font-bold text-brand">逻辑删除</div>
        <div class="mt-2 text-sm text-slate-500">删除后帖子从前台消失，并保留后台操作留痕。</div>
      </div>
      <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
        <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">可见性治理</div>
        <div class="mt-3 text-lg font-bold text-slate-900">隐藏 / 恢复</div>
        <div class="mt-2 text-sm text-slate-500">适合临时屏蔽争议帖，不必立即删除。</div>
      </div>
    </div>

    <div class="flex flex-wrap gap-3 rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
      <el-input v-model="filters.keyword" class="max-w-sm" clearable placeholder="搜索帖子标题/正文" @keyup.enter="$emit('reload')" />
      <el-select v-model="filters.status" clearable placeholder="状态" class="!w-36">
        <el-option label="审核中" :value="0" />
        <el-option label="已发布" :value="1" />
        <el-option label="已驳回" :value="2" />
        <el-option label="已隐藏" :value="3" />
      </el-select>
      <el-select v-model="filters.visibility" clearable placeholder="可见性" class="!w-36">
        <el-option label="可见" :value="1" />
        <el-option label="隐藏" :value="0" />
      </el-select>
      <el-button class="!border-brand/15 !text-brand" @click="$emit('reload')">筛选</el-button>
    </div>

    <div v-for="post in posts" :key="post.id" class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div class="min-w-0">
          <div class="text-lg font-bold text-slate-900">{{ post.title }}</div>
          <div class="mt-2 flex flex-wrap gap-3 text-xs text-slate-400">
            <span>作者 {{ post.authorNickname }}</span>
            <span>{{ formatTime(post.createdAt) }}</span>
            <span>浏览 {{ post.viewCount || 0 }}</span>
            <span>回复 {{ post.replyCount || 0 }}</span>
            <span>点赞 {{ post.likeCount || 0 }}</span>
          </div>
          <div class="mt-3 text-sm leading-7 text-slate-600">{{ post.contentPreview || '暂无帖子摘要。' }}</div>
          <div class="mt-3 flex flex-wrap gap-2">
            <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">{{ postStatusLabel(post.status) }}</span>
            <span :class="post.visibility === 1 ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'" class="rounded-full px-3 py-1 text-xs font-semibold">
              {{ post.visibility === 1 ? '当前可见' : '当前隐藏' }}
            </span>
          </div>
        </div>
        <div class="flex flex-wrap gap-2">
          <el-button class="!border-brand/15 !text-brand" @click="$emit('view-post', post.id)">查看原帖</el-button>
          <el-button class="!border-brand/15 !text-brand" @click="$emit('toggle-visibility', post)">
            {{ post.visibility === 1 ? '隐藏帖子' : '恢复可见' }}
          </el-button>
          <el-button type="danger" class="!border-brand !bg-brand hover:!bg-brand-dark" @click="$emit('delete-post', post)">删除帖子</el-button>
        </div>
      </div>
    </div>

    <div v-if="!posts.length" class="rounded-[24px] bg-slate-50 p-6 text-sm text-slate-500">暂无符合条件的帖子。</div>
  </div>
</template>

<script setup>
defineProps({
  total: { type: Number, default: 0 },
  filters: { type: Object, required: true },
  posts: { type: Array, default: () => [] },
  formatTime: { type: Function, required: true },
  postStatusLabel: { type: Function, required: true }
})

defineEmits(['reload', 'view-post', 'toggle-visibility', 'delete-post'])
</script>
