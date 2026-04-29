<template>
  <div class="rounded-[26px] border border-brand/10 bg-white p-6 shadow-soft">
    <div class="flex flex-wrap items-center gap-3">
      <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Answer</div>
      <span
        class="rounded-full px-3 py-1 text-xs font-semibold"
        :class="loading ? 'bg-sky-50 text-sky-700' : (result?.fallback ? 'bg-amber-50 text-amber-700' : 'bg-emerald-50 text-emerald-700')"
      >
        {{ loading ? '正在生成中' : (result?.fallback ? '知识不足，已兜底' : `命中 ${result?.matchedCount || 0} 条知识`) }}
      </span>
    </div>

    <div v-if="question" class="mt-4 rounded-[18px] bg-slate-50 px-4 py-3 text-sm text-slate-500">
      你的问题：{{ question }}
    </div>

    <div class="mt-5 whitespace-pre-wrap text-[15px] leading-8 text-slate-700">
      {{ result?.answer || '暂时还没有回答内容。' }}
    </div>
  </div>
</template>

<script setup>
defineProps({
  question: { type: String, default: '' },
  result: { type: Object, default: null },
  loading: { type: Boolean, default: false }
})
</script>
