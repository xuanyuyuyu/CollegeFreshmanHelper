<template>
  <div class="space-y-4">
    <div v-for="post in posts" :key="post.id" class="rounded-[24px] bg-white p-5 shadow-soft">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div class="min-w-0">
          <div class="text-lg font-bold text-slate-900">{{ post.title }}</div>
          <div class="mt-2 text-xs text-slate-400">
            {{ formatTime(post.publishedAt || post.createdAt) }} · {{ postStatusLabel(post.status) }} · {{ visibilityLabel(post.visibility) }}
          </div>
          <div class="mt-3 text-sm leading-7 text-slate-600">{{ post.contentPreview || '暂无正文摘要。' }}</div>
        </div>
        <div class="flex flex-wrap gap-2">
          <el-button class="!border-brand/15 !text-brand" @click="$emit('view-post', post.id)">查看详情</el-button>
          <el-button class="!border-rose-200 !text-rose-600" @click="$emit('delete-post', post)">删除帖子</el-button>
        </div>
      </div>
      <div class="mt-4 flex flex-wrap gap-6 text-sm text-slate-500">
        <span>标签 {{ post.tags || '未分类' }}</span>
        <span>评论 {{ post.replyCount || 0 }}</span>
        <span>点赞 {{ post.likeCount || 0 }}</span>
      </div>
    </div>
    <div v-if="!loading && !posts.length" class="rounded-[20px] bg-white p-6 text-sm text-slate-500">
      你还没有发布帖子。
    </div>
  </div>
</template>

<script setup>
defineProps({
  posts: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  formatTime: { type: Function, required: true },
  postStatusLabel: { type: Function, required: true },
  visibilityLabel: { type: Function, required: true }
})

defineEmits(['view-post', 'delete-post'])
</script>
