<template>
  <MainLayout>
    <section class="mx-auto w-full max-w-6xl px-4 py-10 sm:px-6 lg:px-8">
      <div class="grid gap-6 xl:grid-cols-[minmax(0,1.25fr)_360px]">
        <div class="space-y-6">
          <div class="rounded-[32px] border border-brand/10 bg-white p-8 shadow-soft">
            <div class="text-xs font-bold uppercase tracking-[0.35em] text-brand">Assistant</div>
            <h1 class="mt-4 text-4xl font-bold text-slate-900">智能问答</h1>
            <p class="mt-4 max-w-3xl text-base leading-8 text-slate-600">
              第一版问答已经接入站内知识库检索。系统会优先从已启用的知识条目中召回相关内容，再整理成适合新生阅读的答案，并展示来源。
            </p>
          </div>

          <AssistantInputBox
            :form="form"
            :loading="loading"
            @submit="submitQuestion"
            @fill-example="fillExample"
          />

          <AssistantAnswerCard
            v-if="result"
            :question="lastQuestion"
            :result="result"
            :loading="loading"
          />

          <AssistantSourceList
            v-if="result"
            :sources="result.sources || []"
          />
        </div>

        <aside class="space-y-4 xl:sticky xl:top-[104px] xl:self-start">
          <div class="rounded-[26px] border border-brand/10 bg-white p-5 shadow-soft">
            <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Use Tips</div>
            <h3 class="mt-2 text-[22px] font-bold text-slate-900">提问建议</h3>
            <div class="mt-3 space-y-2 text-[13px] leading-6 text-slate-500">
              <p>问题尽量写具体，例如“宿舍晚上几点断电”比“宿舍怎么样”更容易命中。</p>
              <p>如果你知道问题属于哪个场景，可以先选择分类，提高命中率。</p>
              <p>当系统提示知识不足时，建议继续补充场景，或者去论坛发帖咨询。</p>
            </div>
          </div>

          <div class="rounded-[26px] border border-brand/10 bg-white p-5 shadow-soft">
            <div class="text-xs font-bold uppercase tracking-[0.3em] text-brand">Current Scope</div>
            <h3 class="mt-2 text-[22px] font-bold text-slate-900">当前能力边界</h3>
            <div class="mt-3 space-y-2 text-[13px] leading-6 text-slate-500">
              <p>当前版本只基于站内知识库做文本召回，不联网，不读取外部政策网站。</p>
              <p>如果知识库没有覆盖到你的问题，系统会明确告诉你“不足以回答”，而不是强行编造。</p>
              <p>后续会继续升级到论坛飞轮、向量检索和更强的模型回答。</p>
            </div>
          </div>
        </aside>
      </div>
    </section>
  </MainLayout>
</template>

<script setup>
import AssistantAnswerCard from '../components/assistant/AssistantAnswerCard.vue'
import AssistantInputBox from '../components/assistant/AssistantInputBox.vue'
import AssistantSourceList from '../components/assistant/AssistantSourceList.vue'
import { useAssistantChat } from '../composables/useAssistantChat'
import MainLayout from '../layouts/MainLayout.vue'

const {
  form,
  loading,
  result,
  lastQuestion,
  submitQuestion,
  fillExample
} = useAssistantChat()
</script>
