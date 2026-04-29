<template>
  <MainLayout>
    <section class="mx-auto w-full max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <div v-if="!currentUser" class="rounded-[32px] border border-brand/10 bg-white p-10 text-center shadow-soft">
        <div class="text-xs font-bold uppercase tracking-[0.35em] text-brand">Admin Console</div>
        <h1 class="mt-4 text-4xl font-bold text-slate-900">后台管理</h1>
        <p class="mx-auto mt-4 max-w-2xl text-sm leading-7 text-slate-500">登录管理员账号后，可进入后台进行账号治理、内容治理与运营操作。</p>
        <el-button type="danger" class="!mt-8 !border-brand !bg-brand hover:!bg-brand-dark" @click="openAuth('login')">登录管理员账号</el-button>
      </div>

      <div v-else-if="!isAdmin" class="rounded-[32px] border border-brand/10 bg-white p-10 text-center shadow-soft">
        <div class="text-xs font-bold uppercase tracking-[0.35em] text-brand">Admin Console</div>
        <h1 class="mt-4 text-4xl font-bold text-slate-900">后台管理</h1>
        <p class="mx-auto mt-4 max-w-2xl text-sm leading-7 text-slate-500">当前账号不是管理员，无法访问后台管理能力。</p>
      </div>

      <div v-else class="space-y-6">
        <div class="rounded-[32px] border border-brand/10 bg-white p-8 shadow-soft">
          <div class="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <div class="text-xs font-bold uppercase tracking-[0.35em] text-brand">Admin Console</div>
              <h1 class="mt-4 text-4xl font-bold text-slate-900">后台管理</h1>
              <p class="mt-3 max-w-3xl text-sm leading-7 text-slate-500">
                统一处理管理员账号、帖子与评论治理、用户封禁、头衔发放、知识库录入与日志追踪。
              </p>
            </div>
            <div class="flex flex-wrap gap-3">
              <span class="rounded-full bg-brand/8 px-4 py-2 text-xs font-semibold text-brand">{{ adminRoleLabel }}</span>
              <el-button class="!border-brand/15 !text-brand" :loading="loading.any" @click="reloadCurrentTab">刷新当前页</el-button>
            </div>
          </div>
        </div>

        <div class="rounded-[32px] border border-slate-100 bg-white p-5 shadow-soft">
          <el-tabs v-model="activeTab" class="admin-tabs">
            <el-tab-pane v-if="isSuperAdmin" label="管理员账号" name="admins">
              <AdminAccountsTab
                :form="adminCreateForm"
                :filters="adminFilters"
                :loading-create-admin="loading.createAdmin"
                :accounts="adminAccounts"
                :role-label="roleLabel"
                :format-time="formatTime"
                @create-admin="submitCreateAdmin"
                @reload="loadAdminAccounts"
                @toggle-status="toggleAdminStatus"
              />
            </el-tab-pane>

            <el-tab-pane label="帖子管理" name="posts">
              <AdminPostsTab
                :total="postTotal"
                :filters="postFilters"
                :posts="adminPosts"
                :format-time="formatTime"
                :post-status-label="postStatusLabel"
                @reload="loadAdminPosts"
                @view-post="goToPost"
                @toggle-visibility="togglePostVisibility"
                @delete-post="confirmDeletePost"
              />
            </el-tab-pane>

            <el-tab-pane label="评论管理" name="replies">
              <AdminRepliesTab
                :total="replyTotal"
                :filters="replyFilters"
                :replies="adminReplies"
                :format-time="formatTime"
                :post-status-label="postStatusLabel"
                @reload="loadAdminReplies"
                @view-post="goToPost"
                @toggle-visibility="toggleReplyVisibility"
                @delete-reply="confirmDeleteReply"
              />
            </el-tab-pane>

            <el-tab-pane label="用户治理" name="users">
              <AdminUsersTab
                :total="userTotal"
                :filters="userFilters"
                :users="siteUsers"
                :role-label="roleLabel"
                :can-ban-user="canBanUser"
                @reload="loadUserList"
                @ban-user="openBanPrompt"
                @unban-user="handleUnbanUser"
              />
            </el-tab-pane>

            <el-tab-pane label="运营台" name="ops">
              <AdminOpsTab
                :grant-title-form="grantTitleForm"
                :title-options="titleOptions"
                :granted-titles="grantedTitles"
                :knowledge-form="knowledgeForm"
                :knowledge-filters="knowledgeFilters"
                :knowledge-list="knowledgeList"
                :log-filters="logFilters"
                :admin-logs="adminLogs"
                :format-time="formatTime"
                :knowledge-status-label="knowledgeStatusLabel"
                :target-type-label="targetTypeLabel"
                @grant-title="submitGrantTitle"
                @revoke-title="revokeGrantedTitle"
                @create-knowledge="submitKnowledge"
                @reload-knowledge="loadKnowledgeList"
                @change-knowledge-status="changeKnowledgeStatus"
                @reload-logs="loadAdminLogs"
              />
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </section>
  </MainLayout>
</template>

<script setup>
import { onMounted } from 'vue'
import AdminAccountsTab from '../components/admin/AdminAccountsTab.vue'
import AdminOpsTab from '../components/admin/AdminOpsTab.vue'
import AdminPostsTab from '../components/admin/AdminPostsTab.vue'
import AdminRepliesTab from '../components/admin/AdminRepliesTab.vue'
import AdminUsersTab from '../components/admin/AdminUsersTab.vue'
import { useAdminConsole } from '../composables/useAdminConsole'
import MainLayout from '../layouts/MainLayout.vue'
import { useAppShell } from '../stores/appShell'

const { currentUser, ensureShellReady, openAuth } = useAppShell()
const {
  activeTab,
  loading,
  adminAccounts,
  adminPosts,
  adminReplies,
  siteUsers,
  titleOptions,
  grantedTitles,
  knowledgeList,
  adminLogs,
  postTotal,
  replyTotal,
  userTotal,
  adminFilters,
  postFilters,
  replyFilters,
  userFilters,
  knowledgeFilters,
  logFilters,
  adminCreateForm,
  grantTitleForm,
  knowledgeForm,
  isAdmin,
  isSuperAdmin,
  adminRoleLabel,
  roleLabel,
  postStatusLabel,
  knowledgeStatusLabel,
  formatTime,
  targetTypeLabel,
  canBanUser,
  goToPost,
  loadAdminAccounts,
  loadAdminPosts,
  loadAdminReplies,
  loadUserList,
  loadKnowledgeList,
  loadAdminLogs,
  reloadCurrentTab,
  submitCreateAdmin,
  toggleAdminStatus,
  togglePostVisibility,
  confirmDeletePost,
  toggleReplyVisibility,
  confirmDeleteReply,
  openBanPrompt,
  handleUnbanUser,
  submitGrantTitle,
  revokeGrantedTitle,
  submitKnowledge,
  changeKnowledgeStatus
} = useAdminConsole({ currentUser })

onMounted(async () => {
  await ensureShellReady()
  if (isAdmin.value) {
    await reloadCurrentTab()
  }
})
</script>

<style scoped>
:deep(.admin-tabs .el-tabs__item.is-active) {
  color: #8b261e;
}

:deep(.admin-tabs .el-tabs__active-bar) {
  background-color: #8b261e;
}
</style>
