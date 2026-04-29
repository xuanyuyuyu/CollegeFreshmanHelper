<template>
  <MainLayout>
    <section class="mx-auto w-full max-w-6xl px-4 py-12 sm:px-6 lg:px-8">
      <div class="rounded-[32px] border border-brand/10 bg-white p-8 shadow-soft">
        <div v-if="currentUser">
          <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <div class="text-xs font-bold uppercase tracking-[0.35em] text-brand">My Center</div>
              <h1 class="mt-4 text-4xl font-bold text-slate-900">我的</h1>
              <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-500">
                这里集中查看我的资料、帖子、回复和获赞表现，也会展示你当前佩戴中的头衔。
              </p>
            </div>
            <div class="flex flex-wrap gap-3">
              <el-button class="!border-brand/15 !text-brand" :loading="loading.summary || loading.posts || loading.replies || loading.likes || loading.likedItems" @click="reloadAll">
                刷新数据
              </el-button>
              <el-button class="!border-brand/15 !text-brand" @click="openAvatarDialog">修改头像</el-button>
              <el-button type="danger" class="!border-brand !bg-brand hover:!bg-brand-dark" @click="openProfileDialog">
                修改个人信息
              </el-button>
            </div>
          </div>

          <div class="mt-8 grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
            <div class="rounded-[30px] bg-[linear-gradient(135deg,#f7f2f1_0%,#fff_100%)] p-6">
              <div class="flex flex-col gap-5 sm:flex-row sm:items-center">
                <div v-if="summary?.avatarUrl" class="h-20 w-20 overflow-hidden rounded-full border border-white/80 bg-white shadow-soft">
                  <img :src="summary.avatarUrl" alt="avatar" class="h-full w-full object-cover" />
                </div>
                <div v-else class="flex h-20 w-20 items-center justify-center rounded-full bg-white text-2xl font-bold text-brand shadow-soft">
                  {{ (summary?.nickname || currentUser.nickname || '友').slice(0, 1) }}
                </div>
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-3">
                    <div class="text-3xl font-bold text-slate-900">{{ summary?.nickname || currentUser.nickname }}</div>
                    <span
                      class="rounded-full px-3 py-1 text-xs font-semibold"
                      :class="titleBadgeClass(summary?.title)"
                    >
                      {{ summary?.title || '暂无头衔' }}
                    </span>
                  </div>
                  <div class="mt-2 text-sm text-slate-500">账号：{{ summary?.username || currentUser.username }}</div>
                  <div class="mt-1 text-sm text-slate-500">
                    {{ summary?.collegeName || '未填写学院' }} / {{ summary?.majorName || '未填写专业' }}
                  </div>
                </div>
              </div>

              <div class="mt-6 rounded-[24px] bg-white/85 p-5 text-sm leading-7 text-slate-500">
                {{ summary?.bio || '暂未填写个人简介。建议补充你的学院、方向和擅长回答的话题，方便大家快速了解你。' }}
              </div>

              <div class="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                <div v-for="card in statCards" :key="card.label" class="rounded-[22px] bg-white px-4 py-4 shadow-soft">
                  <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">{{ card.label }}</div>
                  <div class="mt-3 text-3xl font-bold text-slate-900">{{ card.value }}</div>
                  <div class="mt-2 text-xs leading-5 text-slate-400">{{ card.tip }}</div>
                </div>
              </div>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <div class="rounded-[24px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-5 shadow-soft">
                <div class="text-base font-semibold text-slate-900">社区表现</div>
                <div class="mt-3 text-sm leading-7 text-slate-500">
                  人工授予头衔会优先展示；未佩戴头衔时，系统会按社区表现展示默认成长头衔。
                </div>
                <div class="mt-4">
                  <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-500">
                    以头衔与积分为主
                  </span>
                </div>
              </div>

              <div class="rounded-[24px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-5 shadow-soft">
                <div class="flex items-center gap-2">
                  <div class="text-base font-semibold text-slate-900">当前头衔</div>
                  <el-popover placement="bottom-start" :width="320" trigger="click">
                    <template #reference>
                      <button
                        type="button"
                        class="inline-flex items-center rounded-full border border-brand/15 bg-brand/5 px-2.5 py-1 text-[11px] font-semibold text-brand transition hover:bg-brand/10"
                      >
                        规则
                      </button>
                    </template>
                    <div class="space-y-3">
                      <div class="text-sm font-semibold text-slate-900">默认成长头衔规则</div>
                      <div class="space-y-2 text-sm leading-6 text-slate-600">
                        <div v-for="rule in titleRules" :key="rule.id">
                          <span class="font-semibold text-slate-900">{{ rule.titleName }}：</span>{{ rule.description || '请以后端规则为准' }}
                        </div>
                        <div v-if="!titleRules.length" class="text-xs text-slate-400">当前没有可用的默认头衔规则。</div>
                        <div class="text-xs text-slate-400">如果当前佩戴了人工或系统授予头衔，会优先显示佩戴头衔。</div>
                      </div>
                    </div>
                  </el-popover>
                </div>
                <div class="mt-3 text-2xl font-bold text-slate-900">{{ summary?.title || '暂未获得' }}</div>
                <div class="mt-2 text-sm leading-7 text-slate-500">
                  这里展示你当前佩戴的头衔；若未佩戴任何头衔，则回退显示默认成长头衔。
                </div>
              </div>

              <div class="rounded-[24px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-5 shadow-soft">
                <div class="text-base font-semibold text-slate-900">院系与年份</div>
                <div class="mt-3 text-sm leading-7 text-slate-500">
                  入学年份：{{ summary?.admissionYear || '未填写' }}<br />
                  性别：{{ genderLabel(summary?.gender) }}
                </div>
              </div>

              <div class="rounded-[24px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-5 shadow-soft">
                <div class="text-base font-semibold text-slate-900">账号状态</div>
                <div class="mt-3 text-sm leading-7 text-slate-500">
                  {{ summary?.status === 1 ? '正常使用' : '当前受限' }}<br />
                  头衔状态：{{ summary?.title || '暂未获得' }}
                </div>
              </div>
            </div>
          </div>

          <div class="mt-8 rounded-[28px] border border-slate-100 bg-slate-50/70 p-4 sm:p-5">
            <el-tabs v-model="activeTab" class="me-tabs">
              <el-tab-pane label="我的帖子" name="posts">
                <MyPostsTab
                  :posts="myPosts"
                  :loading="loading.posts"
                  :format-time="formatTime"
                  :post-status-label="postStatusLabel"
                  :visibility-label="visibilityLabel"
                  @view-post="goToPost"
                  @delete-post="confirmDeleteMyPost"
                />
              </el-tab-pane>

              <el-tab-pane label="我的回复" name="replies">
                <MyRepliesTab
                  :replies="myReplies"
                  :loading="loading.replies"
                  :format-time="formatTime"
                  :reply-status-label="replyStatusLabel"
                  @view-post="goToPost"
                  @delete-reply="confirmDeleteMyReply"
                />
              </el-tab-pane>

              <el-tab-pane label="我的点赞" name="likedItems">
                <MyLikedItemsTab
                  :items="myLikedItems"
                  :loading="loading.likedItems"
                  :format-time="formatTime"
                  :item-status-label="itemStatusLabel"
                  @view-post="goToPost"
                />
              </el-tab-pane>

              <el-tab-pane label="我的获赞" name="likes">
                <MyLikesTab
                  :likes-data="likesData"
                  :like-details="likeDetails"
                  :loading-like-details="loading.likeDetails"
                  :format-time="formatTime"
                  :item-status-label="itemStatusLabel"
                  @reload-like-details="loadLikeDetails"
                  @view-post="goToPost"
                />
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>

        <div v-else class="text-center">
          <div class="text-xs font-bold uppercase tracking-[0.35em] text-brand">My Center</div>
          <h1 class="mt-4 text-4xl font-bold text-slate-900">我的</h1>
          <p class="mx-auto mt-5 max-w-2xl text-base leading-8 text-slate-600">
            登录后可查看我的帖子、我的回复、获赞统计、当前头衔和资料编辑入口。
          </p>
          <el-button type="danger" class="!mt-8 !border-brand !bg-brand hover:!bg-brand-dark" @click="openAuth('login')">
            立即登录
          </el-button>
        </div>
      </div>
    </section>

    <el-dialog v-model="profileDialogVisible" width="520px" title="修改个人信息">
      <div class="space-y-4">
        <el-input v-model="profileForm.nickname" maxlength="32" placeholder="昵称" />
        <div class="grid gap-4 sm:grid-cols-2">
          <el-select v-model="profileForm.gender" placeholder="性别">
            <el-option label="未填写" :value="0" />
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
          <el-input-number v-model="profileForm.admissionYear" :min="2000" :max="2100" class="!w-full" placeholder="入学年份" />
        </div>
        <div class="grid gap-4 sm:grid-cols-2">
          <el-input v-model="profileForm.collegeName" maxlength="64" placeholder="学院名称" />
          <el-input v-model="profileForm.majorName" maxlength="64" placeholder="专业名称" />
        </div>
        <el-input v-model="profileForm.bio" type="textarea" :rows="5" maxlength="255" show-word-limit resize="none" placeholder="个人简介" />
        <el-button
          type="danger"
          class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark"
          :loading="profileSubmitting"
          @click="submitProfile"
        >
          保存修改
        </el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="avatarDialogVisible" width="520px" title="修改头像">
      <div class="space-y-4">
        <div v-if="avatarForm.avatarUrl" class="mx-auto h-24 w-24 overflow-hidden rounded-full border border-slate-100 shadow-soft">
          <img :src="avatarForm.avatarUrl" alt="avatar-preview" class="h-full w-full object-cover" />
        </div>
        <el-input v-model="avatarForm.avatarUrl" placeholder="请输入头像图片 URL" />
        <p class="text-sm leading-6 text-slate-500">
          当前版本先走头像地址更新，后续接入 OSS 直传后可替换为真正的上传流程。
        </p>
        <el-button
          type="danger"
          class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark"
          :loading="avatarSubmitting"
          @click="submitAvatar"
        >
          更新头像
        </el-button>
      </div>
    </el-dialog>
  </MainLayout>
</template>

<script setup>
import { onMounted } from 'vue'
import MainLayout from '../layouts/MainLayout.vue'
import MyLikedItemsTab from '../components/me/MyLikedItemsTab.vue'
import MyLikesTab from '../components/me/MyLikesTab.vue'
import MyPostsTab from '../components/me/MyPostsTab.vue'
import MyRepliesTab from '../components/me/MyRepliesTab.vue'
import { useMyCenter } from '../composables/useMyCenter'
import { useAppShell } from '../stores/appShell'
import { titleBadgeClass } from '../utils/titleBadge'

const { currentUser, ensureShellReady, openAuth, refreshCurrentUser } = useAppShell()
const {
  activeTab,
  summary,
  likesData,
  likeDetails,
  myLikedItems,
  myPosts,
  myReplies,
  profileDialogVisible,
  avatarDialogVisible,
  loading,
  profileSubmitting,
  avatarSubmitting,
  profileForm,
  avatarForm,
  statCards,
  titleRules,
  genderLabel,
  formatTime,
  postStatusLabel,
  visibilityLabel,
  replyStatusLabel,
  itemStatusLabel,
  goToPost,
  openProfileDialog,
  openAvatarDialog,
  loadLikeDetails,
  reloadAll,
  confirmDeleteMyPost,
  confirmDeleteMyReply,
  submitProfile,
  submitAvatar
} = useMyCenter({ currentUser, refreshCurrentUser })

onMounted(async () => {
  await ensureShellReady()
  if (currentUser.value) {
    avatarForm.avatarUrl = currentUser.value.avatarUrl || ''
    await reloadAll()
  }
})
</script>

<style scoped>
:deep(.me-tabs .el-tabs__item.is-active) {
  color: #8b261e;
}

:deep(.me-tabs .el-tabs__active-bar) {
  background-color: #8b261e;
}
</style>
