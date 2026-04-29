<template>
  <div class="grid gap-6 xl:grid-cols-[340px_1fr]">
    <div class="rounded-[28px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-5">
      <div class="text-base font-semibold text-slate-900">新增管理员账号</div>
      <div class="mt-2 text-sm leading-6 text-slate-500">只创建普通管理员账号。超级管理员账号仍建议通过初始化脚本或数据库维护。</div>
      <div class="mt-5 space-y-4">
        <el-input v-model="form.username" maxlength="32" placeholder="管理员账号" />
        <el-input v-model="form.password" type="password" show-password maxlength="32" placeholder="登录密码" />
        <el-input v-model="form.nickname" maxlength="32" placeholder="管理员昵称" />
        <el-button
          type="danger"
          class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark"
          :loading="loadingCreateAdmin"
          @click="$emit('create-admin')"
        >
          添加管理员账号
        </el-button>
      </div>
    </div>

    <div class="space-y-4">
      <div class="flex flex-wrap gap-3 rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
        <el-input v-model="filters.keyword" class="max-w-xs" clearable placeholder="搜索账号/昵称" @keyup.enter="$emit('reload')" />
        <el-select v-model="filters.status" clearable placeholder="状态" class="!w-36">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button class="!border-brand/15 !text-brand" @click="$emit('reload')">筛选</el-button>
      </div>

      <div class="overflow-hidden rounded-[28px] border border-slate-100">
        <div class="overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead class="bg-slate-50 text-slate-500">
              <tr>
                <th class="px-5 py-3 text-left font-semibold">账号</th>
                <th class="px-5 py-3 text-left font-semibold">昵称</th>
                <th class="px-5 py-3 text-left font-semibold">角色</th>
                <th class="px-5 py-3 text-left font-semibold">状态</th>
                <th class="px-5 py-3 text-left font-semibold">创建时间</th>
                <th class="px-5 py-3 text-left font-semibold">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in accounts" :key="user.id" class="border-t border-slate-100">
                <td class="px-5 py-4 text-slate-700">{{ user.username }}</td>
                <td class="px-5 py-4 text-slate-700">{{ user.nickname }}</td>
                <td class="px-5 py-4">
                  <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">{{ roleLabel(user.role) }}</span>
                </td>
                <td class="px-5 py-4">
                  <span :class="user.status === 1 ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'" class="rounded-full px-3 py-1 text-xs font-semibold">
                    {{ user.status === 1 ? '启用中' : '已禁用' }}
                  </span>
                </td>
                <td class="px-5 py-4 text-slate-500">{{ formatTime(user.createdAt) }}</td>
                <td class="px-5 py-4">
                  <el-button
                    v-if="user.role === 8"
                    size="small"
                    class="!border-brand/15 !text-brand"
                    @click="$emit('toggle-status', user)"
                  >
                    {{ user.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <span v-else class="text-xs text-slate-400">超级管理员不可操作</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  form: { type: Object, required: true },
  filters: { type: Object, required: true },
  loadingCreateAdmin: { type: Boolean, default: false },
  accounts: { type: Array, default: () => [] },
  roleLabel: { type: Function, required: true },
  formatTime: { type: Function, required: true }
})

defineEmits(['create-admin', 'reload', 'toggle-status'])
</script>
