<template>
  <MainLayout>
    <section class="mx-auto w-full max-w-5xl px-4 py-12 sm:px-6 lg:px-8">
      <div class="rounded-[32px] border border-brand/10 bg-white p-8 shadow-soft">
        <div class="text-xs font-bold uppercase tracking-[0.35em] text-brand">Assistant</div>
        <h1 class="mt-4 text-4xl font-bold text-slate-900">智能问答</h1>
        <p class="mt-4 text-base leading-8 text-slate-600">
          当前仓库里还没有真正的 RAG 检索和大模型问答接口，所以这里先保留为占位页。后续接入 Qdrant、Embedding 和百炼模型后，可以直接在这个页面完成真实问答。
        </p>
        <div class="mt-8 rounded-[24px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-6">
          <el-input type="textarea" :rows="6" placeholder="后续这里会接真实提问输入框" disabled />
          <el-button type="danger" class="!mt-4 !border-brand !bg-brand hover:!bg-brand-dark" disabled>
            暂未接入
          </el-button>
        </div>
      </div>
    </section>
  </MainLayout>
</template>

<script setup>
import MainLayout from '../layouts/MainLayout.vue'
</script>
