<template>
  <div class="space-y-4">
    <div class="grid gap-4 md:grid-cols-3">
      <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
        <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">筛选结果总数</div>
        <div class="mt-3 text-3xl font-bold text-slate-900">{{ total }}</div>
        <div class="mt-2 text-sm text-slate-500">当前筛选条件下命中的用户总数。</div>
      </div>
      <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
        <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">封禁能力</div>
        <div class="mt-3 text-lg font-bold text-brand">管理员可封禁普通用户</div>
        <div class="mt-2 text-sm text-slate-500">普通管理员不能封禁管理员账号，超级管理员保留最高权限。</div>
      </div>
      <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
        <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">操作留痕</div>
        <div class="mt-3 text-lg font-bold text-slate-900">全量记录</div>
        <div class="mt-2 text-sm text-slate-500">封禁与解封动作都会进入后台日志，便于追踪。</div>
      </div>
    </div>

    <div class="flex flex-wrap gap-3 rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
      <el-input v-model="filters.keyword" class="max-w-sm" clearable placeholder="搜索账号/昵称" @keyup.enter="$emit('reload')" />
      <el-select v-model="filters.role" clearable placeholder="角色" class="!w-36">
        <el-option label="新生" :value="1" />
        <el-option label="老生" :value="2" />
        <el-option label="管理员" :value="8" />
        <el-option label="超级管理员" :value="9" />
      </el-select>
      <el-select v-model="filters.status" clearable placeholder="状态" class="!w-36">
        <el-option label="正常" :value="1" />
        <el-option label="禁用/封禁" :value="0" />
      </el-select>
      <el-button class="!border-brand/15 !text-brand" @click="$emit('reload')">筛选</el-button>
    </div>

    <div class="overflow-hidden rounded-[28px] border border-slate-100">
      <div class="overflow-x-auto">
        <table class="min-w-full text-sm">
          <thead class="bg-slate-50 text-slate-500">
            <tr>
              <th class="px-5 py-3 text-left font-semibold">昵称</th>
              <th class="px-5 py-3 text-left font-semibold">账号</th>
              <th class="px-5 py-3 text-left font-semibold">角色</th>
              <th class="px-5 py-3 text-left font-semibold">状态</th>
              <th class="px-5 py-3 text-left font-semibold">获赞</th>
              <th class="px-5 py-3 text-left font-semibold">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id" class="border-t border-slate-100">
              <td class="px-5 py-4 text-slate-700">{{ user.nickname }}</td>
              <td class="px-5 py-4 text-slate-700">{{ user.username }}</td>
              <td class="px-5 py-4"><span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">{{ roleLabel(user.role) }}</span></td>
              <td class="px-5 py-4">
                <span :class="user.status === 1 ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'" class="rounded-full px-3 py-1 text-xs font-semibold">
                  {{ user.status === 1 ? '正常' : '已禁用/封禁' }}
                </span>
              </td>
              <td class="px-5 py-4 text-slate-500">{{ user.totalLikeReceivedCount || 0 }}</td>
              <td class="px-5 py-4">
                <div class="flex flex-wrap gap-2">
                  <el-button
                    v-if="canBanUser(user) && user.status === 1"
                    size="small"
                    class="!border-brand/15 !text-brand"
                    @click="$emit('ban-user', user)"
                  >
                    封禁用户
                  </el-button>
                  <el-button
                    v-if="canBanUser(user) && user.status !== 1"
                    size="small"
                    class="!border-brand/15 !text-brand"
                    @click="$emit('unban-user', user)"
                  >
                    解封用户
                  </el-button>
                  <span v-if="!canBanUser(user) && user.role !== 9" class="text-xs text-slate-400">当前账号无权处理此用户</span>
                  <span v-if="user.role === 9" class="text-xs text-slate-400">超级管理员不可封禁</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  total: { type: Number, default: 0 },
  filters: { type: Object, required: true },
  users: { type: Array, default: () => [] },
  roleLabel: { type: Function, required: true },
  canBanUser: { type: Function, required: true }
})

defineEmits(['reload', 'ban-user', 'unban-user'])
</script>
