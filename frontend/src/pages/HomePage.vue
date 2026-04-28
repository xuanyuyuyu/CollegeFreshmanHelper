<template>
  <MainLayout>
    <section class="mx-auto w-full max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <div class="overflow-hidden rounded-[40px] border border-brand/10 bg-white shadow-soft">
        <div class="grid gap-0 lg:grid-cols-[1.08fr_0.92fr]">
          <div class="relative overflow-hidden bg-[linear-gradient(135deg,#fff_0%,#f8efed_28%,#f8fafc_100%)] px-8 py-12 sm:px-12 sm:py-14">
            <div class="absolute -right-20 top-10 h-64 w-64 rounded-full bg-brand/5 blur-3xl"></div>
            <div class="absolute -bottom-12 left-12 h-40 w-40 rounded-full bg-slate-200/50 blur-3xl"></div>
            <div class="relative">
              <div class="text-xs font-bold uppercase tracking-[0.35em] text-brand">Freshman Helper</div>
              <h1 class="mt-6 max-w-3xl text-5xl font-bold tracking-tight text-slate-900 sm:text-6xl">
                你好，新同学！
              </h1>
              <p class="mt-6 max-w-2xl text-base leading-8 text-slate-600">
                从报到、住宿、军训到论坛交流和后续智能问答，这里会把新生最常用的信息入口集中到一个地方。
              </p>
              <div class="mt-8 flex flex-wrap gap-4">
                <RouterLink to="/forum" class="rounded-2xl bg-brand px-7 py-4 text-base font-semibold text-white transition hover:bg-brand-dark">
                  进入论坛交流
                </RouterLink>
                <RouterLink to="/assistant" class="rounded-2xl border border-brand/20 bg-white/80 px-7 py-4 text-base font-semibold text-brand transition hover:bg-brand/5">
                  去问问智能问答
                </RouterLink>
              </div>
              <div class="mt-12 grid gap-4 sm:grid-cols-3">
                <div class="rounded-[26px] bg-white/80 p-5 shadow-soft ring-1 ring-white">
                  <div class="text-xs font-bold uppercase tracking-[0.28em] text-brand">Forum</div>
                  <div class="mt-3 text-2xl font-bold text-slate-900">贴吧式交流</div>
                  <div class="mt-2 text-sm leading-7 text-slate-500">先看帖，再进入主楼与楼中楼讨论。</div>
                </div>
                <div class="rounded-[26px] bg-white/80 p-5 shadow-soft ring-1 ring-white">
                  <div class="text-xs font-bold uppercase tracking-[0.28em] text-brand">Guide</div>
                  <div class="mt-3 text-2xl font-bold text-slate-900">入学指引</div>
                  <div class="mt-2 text-sm leading-7 text-slate-500">宿舍、学习、军训、校园生活分类查看。</div>
                </div>
                <div class="rounded-[26px] bg-white/80 p-5 shadow-soft ring-1 ring-white">
                  <div class="text-xs font-bold uppercase tracking-[0.28em] text-brand">Campus</div>
                  <div class="mt-3 text-2xl font-bold text-slate-900">全景校园</div>
                  <div class="mt-2 text-sm leading-7 text-slate-500">通过外部全景入口快速了解校园环境。</div>
                </div>
              </div>
            </div>
          </div>

          <div class="grid gap-4 bg-[linear-gradient(180deg,#f8fafc_0%,#ffffff_100%)] p-8 sm:grid-cols-2 sm:p-10">
            <div class="rounded-[28px] bg-white p-6 shadow-soft">
              <div class="text-sm font-bold uppercase tracking-[0.28em] text-brand">Campus Notes</div>
              <div class="mt-4 text-2xl font-bold text-slate-900">新生最关心</div>
              <div class="mt-3 space-y-2 text-sm leading-7 text-slate-500">
                <div>宿舍床垫需不需要自己准备</div>
                <div>军训时间和强度</div>
                <div>学校食堂和作息</div>
              </div>
            </div>
            <div class="rounded-[28px] bg-brand p-6 text-white shadow-soft">
              <div class="text-sm font-bold uppercase tracking-[0.28em] text-white/75">Community</div>
              <div class="mt-4 text-2xl font-bold">交流先行</div>
              <div class="mt-3 text-sm leading-7 text-white/80">
                当前系统最完整的能力是论坛交流，适合先沉淀新生问题和老生经验。
              </div>
            </div>
            <div class="rounded-[28px] bg-white p-6 shadow-soft sm:col-span-2">
              <div class="flex items-center justify-between gap-4">
                <div>
                  <div class="text-sm font-bold uppercase tracking-[0.28em] text-brand">Status</div>
                  <div class="mt-4 text-2xl font-bold text-slate-900">当前前端已重建</div>
                </div>
                <span class="rounded-full bg-emerald-50 px-4 py-2 text-sm font-semibold text-emerald-700">可运行</span>
              </div>
              <div class="mt-4 text-sm leading-7 text-slate-500">
                这套界面已经按当前后端接口重新生成，优先保证视觉完整度、登录流程和论坛链路能继续演示与迭代。
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </MainLayout>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
</script>
