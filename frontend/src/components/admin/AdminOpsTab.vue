<template>
  <div class="grid gap-6 xl:grid-cols-2">
    <div class="rounded-[28px] border border-slate-100 bg-slate-50/70 p-5">
      <div class="text-base font-semibold text-slate-900">人工授予头衔</div>
      <div class="mt-4 space-y-4">
        <el-input v-model="grantTitleForm.username" placeholder="目标账号" />
        <el-select v-model="grantTitleForm.titleId" placeholder="选择头衔" class="!w-full">
          <el-option v-for="title in titleOptions" :key="title.id" :label="title.titleName" :value="title.id" />
        </el-select>
        <el-switch v-model="grantTitleForm.wearing" active-text="立即佩戴" />
        <el-input v-model="grantTitleForm.remark" maxlength="255" placeholder="发放备注（可选）" />
        <el-button type="danger" class="!w-full !border-brand !bg-brand hover:!bg-brand-dark" @click="$emit('grant-title')">授予头衔</el-button>
      </div>

      <div class="mt-6 border-t border-slate-100 pt-5">
        <div class="mb-3 text-sm font-semibold text-slate-900">已授予头衔记录</div>
        <div class="space-y-3">
          <div v-for="grant in grantedTitles" :key="grant.id" class="rounded-[18px] bg-white p-4">
            <div class="text-sm font-semibold text-slate-900">{{ grant.userNickname || `用户#${grant.userId}` }} · {{ grant.titleName }}</div>
            <div class="mt-1 text-xs text-slate-400">
              {{ grant.isWearing === 1 ? '当前佩戴' : '未佩戴' }} · {{ formatTime(grant.grantedAt) }}
            </div>
            <div v-if="grant.grantRemark" class="mt-2 text-sm text-slate-600">{{ grant.grantRemark }}</div>
            <div class="mt-3 flex gap-2">
              <el-button size="small" class="!border-rose-200 !text-rose-600" @click="$emit('revoke-title', grant)">撤销头衔</el-button>
            </div>
          </div>
          <div v-if="!grantedTitles.length" class="rounded-[18px] bg-white p-4 text-sm text-slate-500">暂无头衔发放记录。</div>
        </div>
      </div>
    </div>

    <div class="rounded-[28px] border border-slate-100 bg-slate-50/70 p-5 xl:col-span-2">
      <div class="grid gap-6 xl:grid-cols-[360px_1fr]">
        <div>
          <div class="text-base font-semibold text-slate-900">手动添加知识库内容</div>
          <div class="mt-4 space-y-4">
            <el-input v-model="knowledgeForm.questionText" maxlength="1000" placeholder="问题" />
            <el-input v-model="knowledgeForm.answerText" type="textarea" :rows="4" placeholder="答案" />
            <div class="grid gap-4 sm:grid-cols-2">
              <el-input v-model="knowledgeForm.category" maxlength="64" placeholder="分类" />
              <el-input v-model="knowledgeForm.rewardPoints" placeholder="贡献积分（选填）" />
            </div>
            <el-button type="danger" class="!w-full !border-brand !bg-brand hover:!bg-brand-dark" @click="$emit('create-knowledge')">写入知识条目</el-button>
          </div>
        </div>

        <div class="space-y-4">
          <div class="flex flex-wrap gap-3 rounded-[24px] border border-slate-100 bg-white/80 p-4">
            <el-input v-model="knowledgeFilters.keyword" class="max-w-sm" clearable placeholder="搜索问题/答案" @keyup.enter="$emit('reload-knowledge')" />
            <el-select v-model="knowledgeFilters.status" clearable placeholder="状态" class="!w-36">
              <el-option label="待向量化" :value="1" />
              <el-option label="已启用" :value="2" />
              <el-option label="已下线" :value="4" />
            </el-select>
            <el-input v-model="knowledgeFilters.category" class="max-w-xs" clearable placeholder="分类" @keyup.enter="$emit('reload-knowledge')" />
            <el-button class="!border-brand/15 !text-brand" @click="$emit('reload-knowledge')">筛选</el-button>
          </div>

          <div class="space-y-3">
            <div v-for="item in knowledgeList" :key="item.id" class="rounded-[20px] bg-white p-4">
              <div class="text-sm font-semibold text-slate-900">{{ item.questionText }}</div>
              <div class="mt-2 text-sm leading-6 text-slate-600 line-clamp-3">{{ item.answerText }}</div>
              <div class="mt-2 flex flex-wrap gap-3 text-xs text-slate-400">
                <span>{{ item.category || '未分类' }}</span>
                <span>{{ knowledgeStatusLabel(item.status) }}</span>
                <span>{{ formatTime(item.createdAt) }}</span>
              </div>
              <div class="mt-3 flex flex-wrap gap-2">
                <el-button v-if="item.status !== 2" size="small" class="!border-brand/15 !text-brand" @click="$emit('change-knowledge-status', item, 2)">设为启用</el-button>
                <el-button v-if="item.status !== 1" size="small" class="!border-brand/15 !text-brand" @click="$emit('change-knowledge-status', item, 1)">设为待向量化</el-button>
                <el-button v-if="item.status !== 4" size="small" class="!border-rose-200 !text-rose-600" @click="$emit('change-knowledge-status', item, 4)">下线</el-button>
              </div>
            </div>
            <div v-if="!knowledgeList.length" class="rounded-[20px] bg-white p-4 text-sm text-slate-500">暂无知识库条目。</div>
          </div>
        </div>
      </div>
    </div>

    <div class="rounded-[28px] border border-slate-100 bg-slate-50/70 p-5 xl:col-span-2">
      <div class="flex flex-wrap items-center gap-3">
        <div class="text-base font-semibold text-slate-900">操作日志</div>
        <el-select v-model="logFilters.targetType" clearable placeholder="目标类型" class="!w-36">
          <el-option label="用户" :value="1" />
          <el-option label="帖子" :value="2" />
          <el-option label="评论" :value="3" />
          <el-option label="知识库" :value="4" />
          <el-option label="头衔" :value="6" />
        </el-select>
        <el-input v-model="logFilters.operationType" class="max-w-xs" clearable placeholder="操作类型，如 DELETE_POST" @keyup.enter="$emit('reload-logs')" />
        <el-button class="!border-brand/15 !text-brand" @click="$emit('reload-logs')">筛选</el-button>
      </div>

      <div class="mt-4 grid gap-3 md:grid-cols-2">
        <div v-for="log in adminLogs" :key="log.id" class="rounded-[20px] bg-white p-4">
          <div class="text-sm font-semibold text-slate-900">{{ log.operationType }}</div>
          <div class="mt-1 text-xs text-slate-400">{{ log.adminNickname || `管理员#${log.adminUserId}` }} · {{ formatTime(log.createdAt) }}</div>
          <div class="mt-2 text-sm text-slate-600">目标 {{ targetTypeLabel(log.targetType) }} #{{ log.targetId }}</div>
          <div v-if="log.reason" class="mt-1 text-xs text-slate-400">备注：{{ log.reason }}</div>
        </div>
        <div v-if="!adminLogs.length" class="rounded-[20px] bg-white p-4 text-sm text-slate-500">暂无日志。</div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  grantTitleForm: { type: Object, required: true },
  titleOptions: { type: Array, default: () => [] },
  grantedTitles: { type: Array, default: () => [] },
  knowledgeForm: { type: Object, required: true },
  knowledgeFilters: { type: Object, required: true },
  knowledgeList: { type: Array, default: () => [] },
  logFilters: { type: Object, required: true },
  adminLogs: { type: Array, default: () => [] },
  formatTime: { type: Function, required: true },
  knowledgeStatusLabel: { type: Function, required: true },
  targetTypeLabel: { type: Function, required: true }
})

defineEmits([
  'grant-title',
  'revoke-title',
  'create-knowledge',
  'reload-knowledge',
  'change-knowledge-status',
  'reload-logs'
])
</script>
