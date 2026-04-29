<template>
  <div>
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
      <div class="rounded-[24px] bg-white p-5 shadow-soft lg:col-span-2">
        <div class="text-sm font-semibold text-slate-900">当前成长参考</div>
        <div class="mt-3 text-sm leading-7 text-slate-600">
          持续积累获赞、精选回答和知识贡献后，可逐步达到更高层级的答疑头衔。
        </div>
      </div>
    </div>
    <div class="mt-5 rounded-[24px] bg-white p-5 shadow-soft">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div class="text-base font-semibold text-slate-900">获赞明细</div>
          <div class="mt-1 text-sm text-slate-500">按点赞数优先展示你的高反馈帖子和回复，便于快速回看高价值内容。</div>
        </div>
        <el-button
          class="!border-brand/15 !text-brand"
          :loading="loadingLikeDetails"
          @click="$emit('reload-like-details')"
        >
          刷新明细
        </el-button>
      </div>

      <div class="mt-4 space-y-4">
        <div v-for="item in likeDetails" :key="`${item.targetType}-${item.targetId}`" class="rounded-[20px] border border-slate-100 p-4">
          <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div class="min-w-0">
              <div class="flex flex-wrap items-center gap-2">
                <span
                  class="rounded-full px-2.5 py-1 text-xs font-semibold"
                  :class="item.targetType === 'POST' ? 'bg-brand/10 text-brand' : 'bg-slate-100 text-slate-600'"
                >
                  {{ item.targetType === 'POST' ? '帖子' : '回复' }}
                </span>
                <div class="truncate text-sm font-semibold text-slate-900">
                  {{ item.targetTitle || '未命名内容' }}
                </div>
              </div>
              <div class="mt-2 text-xs text-slate-400">
                {{ formatTime(item.createdAt) }} · {{ itemStatusLabel(item.status, item.visibility) }}
              </div>
              <div class="mt-3 line-clamp-3 text-sm leading-7 text-slate-600">
                {{ item.contentPreview || '暂无内容摘要。' }}
              </div>
              <div v-if="item.postTitle" class="mt-3 text-xs text-slate-500">
                所在帖子：{{ item.postTitle }}
              </div>
            </div>
            <div class="flex items-center gap-3">
              <div class="rounded-[18px] bg-slate-50 px-4 py-3 text-center">
                <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">获赞</div>
                <div class="mt-2 text-2xl font-bold text-slate-900">{{ item.likeCount || 0 }}</div>
              </div>
              <el-button class="!border-brand/15 !text-brand" @click="$emit('view-post', item.postId)">查看原帖</el-button>
            </div>
          </div>
        </div>
        <div v-if="!loadingLikeDetails && !likeDetails.length" class="rounded-[20px] bg-slate-50 p-6 text-sm text-slate-500">
          你目前还没有获赞明细，先去论坛参与发帖或答疑吧。
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  likesData: { type: Object, default: null },
  likeDetails: { type: Array, default: () => [] },
  loadingLikeDetails: { type: Boolean, default: false },
  formatTime: { type: Function, required: true },
  itemStatusLabel: { type: Function, required: true }
})

defineEmits(['reload-like-details', 'view-post'])
</script>
