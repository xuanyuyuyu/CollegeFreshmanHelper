<template>
  <div class="space-y-4">
    <div v-for="item in items" :key="`${item.targetType}-${item.targetId}`" class="rounded-[24px] bg-white p-5 shadow-soft">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div class="min-w-0">
          <div class="flex flex-wrap items-center gap-2">
            <span
              class="rounded-full px-2.5 py-1 text-xs font-semibold"
              :class="item.targetType === 'POST' ? 'bg-brand/10 text-brand' : 'bg-slate-100 text-slate-600'"
            >
              {{ item.targetType === 'POST' ? '点赞的帖子' : '点赞的回复' }}
            </span>
            <div class="truncate text-sm font-semibold text-slate-900">
              {{ item.targetTitle || '内容已不可见' }}
            </div>
          </div>
          <div class="mt-2 text-xs text-slate-400">
            点赞于 {{ formatTime(item.likedAt) }} · {{ itemStatusLabel(item.status, item.visibility) }}
          </div>
          <div class="mt-3 text-sm leading-7 text-slate-600">
            {{ item.contentPreview || '暂无内容摘要。' }}
          </div>
          <div v-if="item.postTitle" class="mt-3 text-xs text-slate-500">
            所在帖子：{{ item.postTitle }}
          </div>
        </div>
        <div class="flex items-center gap-3">
          <div class="rounded-[18px] bg-slate-50 px-4 py-3 text-center">
            <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">当前总赞</div>
            <div class="mt-2 text-2xl font-bold text-slate-900">{{ item.likeCount || 0 }}</div>
          </div>
          <el-button class="!border-brand/15 !text-brand" @click="$emit('view-post', item.postId)">查看原帖</el-button>
        </div>
      </div>
    </div>
    <div v-if="!loading && !items.length" class="rounded-[20px] bg-white p-6 text-sm text-slate-500">
      你还没有点赞过帖子或回复。
    </div>
  </div>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  formatTime: { type: Function, required: true },
  itemStatusLabel: { type: Function, required: true }
})

defineEmits(['view-post'])
</script>
