<template>
  <div class="rounded-[26px] border border-brand/10 bg-white p-6 shadow-soft">
    <div class="flex items-center justify-between gap-3">
      <div>
        <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Sources</div>
        <h3 class="mt-2 text-[24px] font-bold text-slate-900">回答来源</h3>
      </div>
      <div class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-500">
        {{ sources.length }} 条
      </div>
    </div>

    <div class="mt-5 space-y-4">
      <div v-for="source in sources" :key="source.knowledgeId" class="rounded-[20px] bg-slate-50/80 p-4">
        <div class="flex flex-wrap items-center gap-2">
          <span class="rounded-full bg-brand/10 px-2.5 py-1 text-xs font-semibold text-brand">
            #{{ source.knowledgeId }}
          </span>
          <span v-if="source.category" class="rounded-full bg-white px-2.5 py-1 text-xs font-semibold text-slate-500">
            {{ source.category }}
          </span>
          <span class="rounded-full bg-white px-2.5 py-1 text-xs font-semibold text-slate-500">
            {{ source.sourceLabel }}
          </span>
        </div>

        <div class="mt-3 text-sm font-semibold text-slate-900">
          {{ source.questionText }}
        </div>
        <div class="mt-2 text-sm leading-7 text-slate-600">
          {{ source.answerText }}
        </div>

        <div class="mt-3 flex flex-wrap gap-3 text-xs text-slate-400">
          <span v-if="source.sourcePostId">来源帖子 ID：{{ source.sourcePostId }}</span>
          <span v-if="source.sourceReplyId">来源回复 ID：{{ source.sourceReplyId }}</span>
          <span v-if="source.contributorUserId">贡献用户 ID：{{ source.contributorUserId }}</span>
        </div>
      </div>

      <div v-if="!sources.length" class="rounded-[20px] bg-slate-50 p-5 text-sm text-slate-500">
        当前没有可展示的来源。
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  sources: { type: Array, default: () => [] }
})
</script>
