<template>
  <div class="space-y-4">
    <div class="grid gap-4 md:grid-cols-3">
      <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
        <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">筛选结果总数</div>
        <div class="mt-3 text-3xl font-bold text-slate-900">{{ total }}</div>
        <div class="mt-2 text-sm text-slate-500">当前筛选条件下命中的评论总数。</div>
      </div>
      <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
        <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">删除评论</div>
        <div class="mt-3 text-lg font-bold text-brand">一键治理</div>
        <div class="mt-2 text-sm text-slate-500">适合处理明显违规、引战或无意义灌水回复。</div>
      </div>
      <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
        <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">临时处理</div>
        <div class="mt-3 text-lg font-bold text-slate-900">隐藏优先</div>
        <div class="mt-2 text-sm text-slate-500">先隐藏，确认后再删除，能降低误删风险。</div>
      </div>
    </div>

    <div class="flex flex-wrap gap-3 rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
      <el-input v-model="filters.keyword" class="max-w-sm" clearable placeholder="搜索评论内容 / 作者 / 帖子标题" @keyup.enter="$emit('reload')" />
      <el-input v-model="filters.postId" class="max-w-xs" clearable placeholder="按帖子 ID 筛选" />
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

    <div v-for="reply in replies" :key="reply.id" class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div class="min-w-0">
          <div class="text-sm font-semibold text-brand">帖子：{{ reply.postTitle || `#${reply.postId}` }}</div>
          <div class="mt-2 flex flex-wrap gap-3 text-xs text-slate-400">
            <span>{{ reply.authorNickname }}</span>
            <span>{{ formatTime(reply.createdAt) }}</span>
            <span>点赞 {{ reply.likeCount || 0 }}</span>
            <span v-if="reply.replyToUserNickname">回复 {{ reply.replyToUserNickname }}</span>
          </div>
          <div class="mt-3 text-sm leading-7 text-slate-600">{{ reply.contentPreview || '暂无评论摘要。' }}</div>
          <div class="mt-3 flex flex-wrap gap-2">
            <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">{{ postStatusLabel(reply.status) }}</span>
            <span :class="reply.visibility === 1 ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'" class="rounded-full px-3 py-1 text-xs font-semibold">
              {{ reply.visibility === 1 ? '当前可见' : '当前隐藏' }}
            </span>
          </div>
        </div>
        <div class="flex flex-wrap gap-2">
          <el-button class="!border-brand/15 !text-brand" @click="$emit('view-post', reply.postId)">查看帖子</el-button>
          <el-button class="!border-brand/15 !text-brand" @click="$emit('toggle-visibility', reply)">
            {{ reply.visibility === 1 ? '隐藏评论' : '恢复可见' }}
          </el-button>
          <el-button type="danger" class="!border-brand !bg-brand hover:!bg-brand-dark" @click="$emit('delete-reply', reply)">删除评论</el-button>
        </div>
      </div>
    </div>

    <div v-if="!replies.length" class="rounded-[24px] bg-slate-50 p-6 text-sm text-slate-500">暂无符合条件的评论。</div>
  </div>
</template>

<script setup>
defineProps({
  total: { type: Number, default: 0 },
  filters: { type: Object, required: true },
  replies: { type: Array, default: () => [] },
  formatTime: { type: Function, required: true },
  postStatusLabel: { type: Function, required: true }
})

defineEmits(['reload', 'view-post', 'toggle-visibility', 'delete-reply'])
</script>
