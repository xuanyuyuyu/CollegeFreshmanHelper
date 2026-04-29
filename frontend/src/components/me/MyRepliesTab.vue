<template>
  <div class="space-y-4">
    <div v-for="reply in replies" :key="reply.id" class="rounded-[24px] bg-white p-5 shadow-soft">
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
        <div class="flex flex-wrap gap-2">
          <el-button class="!border-brand/15 !text-brand" @click="$emit('view-post', reply.postId)">去原帖</el-button>
          <el-button class="!border-rose-200 !text-rose-600" @click="$emit('delete-reply', reply)">删除回复</el-button>
        </div>
      </div>
      <div class="mt-4 flex flex-wrap gap-6 text-sm text-slate-500">
        <span>点赞 {{ reply.likeCount || 0 }}</span>
        <span>子回复 {{ reply.childCount || 0 }}</span>
      </div>
    </div>
    <div v-if="!loading && !replies.length" class="rounded-[20px] bg-white p-6 text-sm text-slate-500">
      你还没有发表回复。
    </div>
  </div>
</template>

<script setup>
defineProps({
  replies: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  formatTime: { type: Function, required: true },
  replyStatusLabel: { type: Function, required: true }
})

defineEmits(['view-post', 'delete-reply'])
</script>
