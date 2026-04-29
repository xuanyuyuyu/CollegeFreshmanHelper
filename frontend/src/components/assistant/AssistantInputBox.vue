<template>
    <div class="rounded-[26px] border border-brand/10 bg-[linear-gradient(180deg,#fff_0%,#fbf7f6_100%)] p-5 shadow-soft">
    <div class="flex flex-col gap-4">
      <div class="flex flex-wrap gap-2">
        <button
          v-for="example in examples"
          :key="example.question"
          type="button"
          class="rounded-full border border-brand/10 bg-white px-3 py-1.5 text-xs font-semibold text-slate-500 transition hover:border-brand/25 hover:text-brand"
          @click="$emit('fill-example', example.question)"
        >
          {{ example.label }}
        </button>
      </div>

      <el-input
        v-model="form.question"
        type="textarea"
        :rows="5"
        resize="none"
        placeholder="例如：军训鞋垫需要自己准备吗？宿舍晚上几点断电？食堂饭卡怎么充？"
        @keyup.ctrl.enter="$emit('submit')"
      />

      <div class="flex items-center justify-between gap-4">
        <p class="text-sm leading-6 text-slate-500">第一版问答目前基于知识库文本召回，不会联网搜索。`Ctrl + Enter` 可快捷提问。</p>
        <el-button
          type="danger"
          class="!border-brand !bg-brand !font-semibold hover:!bg-brand-dark"
          :loading="loading"
          @click="$emit('submit')"
        >
          开始提问
        </el-button>
      </div>
      </div>
    </div>
</template>

<script setup>
defineProps({
  form: { type: Object, required: true },
  loading: { type: Boolean, default: false }
})

defineEmits(['submit', 'fill-example'])

const examples = [
  { label: '军训准备', question: '军训鞋垫需要自己准备吗？' },
  { label: '宿舍作息', question: '宿舍晚上几点断电？' },
  { label: '食堂就餐', question: '食堂饭卡怎么充值？' },
  { label: '学习安排', question: '大一刚开学需要买很多教材吗？' }
]
</script>
