<template>
  <MainLayout>
    <section class="mx-auto w-full max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <div class="rounded-[32px] border border-brand/10 bg-white p-8 shadow-soft">
        <div class="text-xs font-bold uppercase tracking-[0.35em] text-brand">Guide</div>
        <h1 class="mt-4 text-4xl font-bold text-slate-900">入学指南</h1>
        <div class="mt-8 grid gap-5 md:grid-cols-2 xl:grid-cols-4">
          <div v-for="item in guideItems" :key="item.title" class="rounded-[24px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-6 shadow-soft">
            <div class="text-xl font-bold text-slate-900">{{ item.title }}</div>
            <div class="mt-3 text-sm leading-7 text-slate-500">{{ item.text }}</div>
          </div>
        </div>
      </div>
    </section>
  </MainLayout>
</template>

<script setup>
import MainLayout from '../layouts/MainLayout.vue'

const guideItems = [
  { title: '报到准备', text: '提前确认录取通知书、身份证、照片、银行卡和缴费流程。' },
  { title: '宿舍入住', text: '优先确认床上用品、插座、洗漱用品和宿舍管理规则。' },
  { title: '军训事项', text: '准备防晒、运动鞋、常备药，并留意学校后续军训通知。' },
  { title: '学习生活', text: '尽快熟悉学院、专业课程、图书馆和食堂等核心生活场景。' }
]
</script>
