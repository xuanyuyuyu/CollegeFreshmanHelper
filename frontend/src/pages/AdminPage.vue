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
              <div class="grid gap-6 xl:grid-cols-[340px_1fr]">
                <div class="rounded-[28px] bg-[linear-gradient(180deg,#f8fafc_0%,#fff_100%)] p-5">
                  <div class="text-base font-semibold text-slate-900">新增管理员账号</div>
                  <div class="mt-2 text-sm leading-6 text-slate-500">只创建普通管理员账号。超级管理员账号仍建议通过初始化脚本或数据库维护。</div>
                  <div class="mt-5 space-y-4">
                    <el-input v-model="adminCreateForm.username" maxlength="32" placeholder="管理员账号" />
                    <el-input v-model="adminCreateForm.password" type="password" show-password maxlength="32" placeholder="登录密码" />
                    <el-input v-model="adminCreateForm.nickname" maxlength="32" placeholder="管理员昵称" />
                    <el-button
                      type="danger"
                      class="!h-11 !w-full !border-brand !bg-brand !font-semibold hover:!bg-brand-dark"
                      :loading="loading.createAdmin"
                      @click="submitCreateAdmin"
                    >
                      添加管理员账号
                    </el-button>
                  </div>
                </div>

                <div class="space-y-4">
                  <div class="flex flex-wrap gap-3 rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
                    <el-input v-model="adminFilters.keyword" class="max-w-xs" clearable placeholder="搜索账号/昵称" @keyup.enter="loadAdminAccounts" />
                    <el-select v-model="adminFilters.status" clearable placeholder="状态" class="!w-36">
                      <el-option label="启用" :value="1" />
                      <el-option label="禁用" :value="0" />
                    </el-select>
                    <el-button class="!border-brand/15 !text-brand" @click="loadAdminAccounts">筛选</el-button>
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
                          <tr v-for="user in adminAccounts" :key="user.id" class="border-t border-slate-100">
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
                                @click="toggleAdminStatus(user)"
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
            </el-tab-pane>

            <el-tab-pane label="帖子管理" name="posts">
              <div class="space-y-4">
                <div class="grid gap-4 md:grid-cols-3">
                  <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
                    <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">筛选结果总数</div>
                    <div class="mt-3 text-3xl font-bold text-slate-900">{{ postTotal }}</div>
                    <div class="mt-2 text-sm text-slate-500">当前筛选条件下命中的帖子总数。</div>
                  </div>
                  <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
                    <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">删除动作</div>
                    <div class="mt-3 text-lg font-bold text-brand">逻辑删除</div>
                    <div class="mt-2 text-sm text-slate-500">删除后帖子从前台消失，并保留后台操作留痕。</div>
                  </div>
                  <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
                    <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">可见性治理</div>
                    <div class="mt-3 text-lg font-bold text-slate-900">隐藏 / 恢复</div>
                    <div class="mt-2 text-sm text-slate-500">适合临时屏蔽争议帖，不必立即删除。</div>
                  </div>
                </div>

                <div class="flex flex-wrap gap-3 rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
                  <el-input v-model="postFilters.keyword" class="max-w-sm" clearable placeholder="搜索帖子标题/正文" @keyup.enter="loadAdminPosts" />
                  <el-select v-model="postFilters.status" clearable placeholder="状态" class="!w-36">
                    <el-option label="审核中" :value="0" />
                    <el-option label="已发布" :value="1" />
                    <el-option label="已驳回" :value="2" />
                    <el-option label="已隐藏" :value="3" />
                  </el-select>
                  <el-select v-model="postFilters.visibility" clearable placeholder="可见性" class="!w-36">
                    <el-option label="可见" :value="1" />
                    <el-option label="隐藏" :value="0" />
                  </el-select>
                  <el-button class="!border-brand/15 !text-brand" @click="loadAdminPosts">筛选</el-button>
                </div>

                <div v-for="post in adminPosts" :key="post.id" class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
                  <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                    <div class="min-w-0">
                      <div class="text-lg font-bold text-slate-900">{{ post.title }}</div>
                      <div class="mt-2 flex flex-wrap gap-3 text-xs text-slate-400">
                        <span>作者 {{ post.authorNickname }}</span>
                        <span>{{ formatTime(post.createdAt) }}</span>
                        <span>浏览 {{ post.viewCount || 0 }}</span>
                        <span>回复 {{ post.replyCount || 0 }}</span>
                        <span>点赞 {{ post.likeCount || 0 }}</span>
                      </div>
                      <div class="mt-3 text-sm leading-7 text-slate-600">{{ post.contentPreview || '暂无帖子摘要。' }}</div>
                      <div class="mt-3 flex flex-wrap gap-2">
                        <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">{{ postStatusLabel(post.status) }}</span>
                        <span :class="post.visibility === 1 ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'" class="rounded-full px-3 py-1 text-xs font-semibold">
                          {{ post.visibility === 1 ? '当前可见' : '当前隐藏' }}
                        </span>
                      </div>
                    </div>
                    <div class="flex flex-wrap gap-2">
                      <el-button class="!border-brand/15 !text-brand" @click="goToPost(post.id)">查看原帖</el-button>
                      <el-button class="!border-brand/15 !text-brand" @click="togglePostVisibility(post)">
                        {{ post.visibility === 1 ? '隐藏帖子' : '恢复可见' }}
                      </el-button>
                      <el-button type="danger" class="!border-brand !bg-brand hover:!bg-brand-dark" @click="confirmDeletePost(post)">删除帖子</el-button>
                    </div>
                  </div>
                </div>

                <div v-if="!adminPosts.length" class="rounded-[24px] bg-slate-50 p-6 text-sm text-slate-500">暂无符合条件的帖子。</div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="评论管理" name="replies">
              <div class="space-y-4">
                <div class="grid gap-4 md:grid-cols-3">
                  <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
                    <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">筛选结果总数</div>
                    <div class="mt-3 text-3xl font-bold text-slate-900">{{ replyTotal }}</div>
                    <div class="mt-2 text-sm text-slate-500">当前筛选条件下命中的评论总数。</div>
                  </div>
                  <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
                    <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">删除评论</div>
                    <div class="mt-3 text-lg font-bold text-brand">一键治理</div>
                    <div class="mt-2 text-sm text-slate-500">适合处理明显违规、引战或无意义灌水回复。</div>
                  </div>
                  <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
                    <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">临时处理</div>
                    <div class="mt-3 text-lg font-bold text-slate-900">隐藏优先</div>
                    <div class="mt-2 text-sm text-slate-500">先隐藏，确认后再删除，能降低误删风险。</div>
                  </div>
                </div>

                <div class="flex flex-wrap gap-3 rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
                  <el-input v-model="replyFilters.keyword" class="max-w-sm" clearable placeholder="搜索评论内容 / 作者 / 帖子标题" @keyup.enter="loadAdminReplies" />
                  <el-input v-model="replyFilters.postId" class="max-w-xs" clearable placeholder="按帖子 ID 筛选" />
                  <el-select v-model="replyFilters.status" clearable placeholder="状态" class="!w-36">
                    <el-option label="审核中" :value="0" />
                    <el-option label="已发布" :value="1" />
                    <el-option label="已驳回" :value="2" />
                    <el-option label="已隐藏" :value="3" />
                  </el-select>
                  <el-select v-model="replyFilters.visibility" clearable placeholder="可见性" class="!w-36">
                    <el-option label="可见" :value="1" />
                    <el-option label="隐藏" :value="0" />
                  </el-select>
                  <el-button class="!border-brand/15 !text-brand" @click="loadAdminReplies">筛选</el-button>
                </div>

                <div v-for="reply in adminReplies" :key="reply.id" class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
                  <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                    <div class="min-w-0">
                      <div class="text-sm font-semibold text-brand">帖子：{{ reply.postTitle || `#${reply.postId}` }}</div>
                      <div class="mt-2 flex flex-wrap gap-3 text-xs text-slate-400">
                        <span>{{ reply.authorNickname }}</span>
                        <span>{{ formatTime(reply.createdAt) }}</span>
                        <span>点赞 {{ reply.likeCount || 0 }}</span>
                        <span v-if="reply.replyToUserNickname">回复 {{ reply.replyToUserNickname }}</span>
                      </div>
                      <div class="mt-3 text-sm leading-7 text-slate-600">{{ reply.contentPreview || '暂无评论摘要。' }}</div>
                      <div class="mt-3 flex flex-wrap gap-2">
                        <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">{{ postStatusLabel(reply.status) }}</span>
                        <span :class="reply.visibility === 1 ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'" class="rounded-full px-3 py-1 text-xs font-semibold">
                          {{ reply.visibility === 1 ? '当前可见' : '当前隐藏' }}
                        </span>
                      </div>
                    </div>
                    <div class="flex flex-wrap gap-2">
                      <el-button class="!border-brand/15 !text-brand" @click="goToPost(reply.postId)">查看帖子</el-button>
                      <el-button class="!border-brand/15 !text-brand" @click="toggleReplyVisibility(reply)">
                        {{ reply.visibility === 1 ? '隐藏评论' : '恢复可见' }}
                      </el-button>
                      <el-button type="danger" class="!border-brand !bg-brand hover:!bg-brand-dark" @click="confirmDeleteReply(reply)">删除评论</el-button>
                    </div>
                  </div>
                </div>

                <div v-if="!adminReplies.length" class="rounded-[24px] bg-slate-50 p-6 text-sm text-slate-500">暂无符合条件的评论。</div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="用户治理" name="users">
              <div class="space-y-4">
                <div class="grid gap-4 md:grid-cols-3">
                  <div class="rounded-[24px] border border-slate-100 bg-slate-50/70 p-5">
                    <div class="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">筛选结果总数</div>
                    <div class="mt-3 text-3xl font-bold text-slate-900">{{ userTotal }}</div>
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
                  <el-input v-model="userFilters.keyword" class="max-w-sm" clearable placeholder="搜索账号/昵称" @keyup.enter="loadUserList" />
                  <el-select v-model="userFilters.role" clearable placeholder="角色" class="!w-36">
                    <el-option label="新生" :value="1" />
                    <el-option label="老生" :value="2" />
                    <el-option label="管理员" :value="8" />
                    <el-option label="超级管理员" :value="9" />
                  </el-select>
                  <el-select v-model="userFilters.status" clearable placeholder="状态" class="!w-36">
                    <el-option label="正常" :value="1" />
                    <el-option label="禁用/封禁" :value="0" />
                  </el-select>
                  <el-button class="!border-brand/15 !text-brand" @click="loadUserList">筛选</el-button>
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
                        <tr v-for="user in siteUsers" :key="user.id" class="border-t border-slate-100">
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
                                @click="openBanPrompt(user)"
                              >
                                封禁用户
                              </el-button>
                              <el-button
                                v-if="canBanUser(user) && user.status !== 1"
                                size="small"
                                class="!border-brand/15 !text-brand"
                                @click="handleUnbanUser(user)"
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
            </el-tab-pane>

            <el-tab-pane label="运营台" name="ops">
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
                    <el-button type="danger" class="!w-full !border-brand !bg-brand hover:!bg-brand-dark" @click="submitGrantTitle">授予头衔</el-button>
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
                          <el-button size="small" class="!border-rose-200 !text-rose-600" @click="revokeGrantedTitle(grant)">撤销头衔</el-button>
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
                        <el-button type="danger" class="!w-full !border-brand !bg-brand hover:!bg-brand-dark" @click="submitKnowledge">写入知识条目</el-button>
                      </div>
                    </div>

                    <div class="space-y-4">
                      <div class="flex flex-wrap gap-3 rounded-[24px] border border-slate-100 bg-white/80 p-4">
                        <el-input v-model="knowledgeFilters.keyword" class="max-w-sm" clearable placeholder="搜索问题/答案" @keyup.enter="loadKnowledgeList" />
                        <el-select v-model="knowledgeFilters.status" clearable placeholder="状态" class="!w-36">
                          <el-option label="待向量化" :value="1" />
                          <el-option label="已启用" :value="2" />
                          <el-option label="已下线" :value="4" />
                        </el-select>
                        <el-input v-model="knowledgeFilters.category" class="max-w-xs" clearable placeholder="分类" @keyup.enter="loadKnowledgeList" />
                        <el-button class="!border-brand/15 !text-brand" @click="loadKnowledgeList">筛选</el-button>
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
                            <el-button v-if="item.status !== 2" size="small" class="!border-brand/15 !text-brand" @click="changeKnowledgeStatus(item, 2)">设为启用</el-button>
                            <el-button v-if="item.status !== 1" size="small" class="!border-brand/15 !text-brand" @click="changeKnowledgeStatus(item, 1)">设为待向量化</el-button>
                            <el-button v-if="item.status !== 4" size="small" class="!border-rose-200 !text-rose-600" @click="changeKnowledgeStatus(item, 4)">下线</el-button>
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
                    <el-input v-model="logFilters.operationType" class="max-w-xs" clearable placeholder="操作类型，如 DELETE_POST" @keyup.enter="loadAdminLogs" />
                    <el-button class="!border-brand/15 !text-brand" @click="loadAdminLogs">筛选</el-button>
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
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </section>
  </MainLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import MainLayout from '../layouts/MainLayout.vue'
import {
  banUser,
  createAdminUser,
  createKnowledge,
  deleteAdminPost,
  deleteAdminReply,
  fetchAdminLogs,
  fetchAdminPosts,
  fetchAdminReplies,
  fetchAdminUsers,
  fetchGrantedTitles,
  fetchKnowledgeList,
  fetchTitles,
  grantTitle,
  revokeTitle,
  unbanUser,
  updateAdminPostVisibility,
  updateAdminReplyVisibility,
  updateAdminUserStatus,
  updateKnowledgeStatus
} from '../api/admin'
import { useAppShell } from '../stores/appShell'

const router = useRouter()
const { currentUser, ensureShellReady, openAuth } = useAppShell()
const LIST_FETCH_SIZE = 100

const activeTab = ref('posts')

const loading = reactive({
  any: false,
  createAdmin: false
})

const adminAccounts = ref([])
const adminPosts = ref([])
const adminReplies = ref([])
const siteUsers = ref([])
const titleOptions = ref([])
const grantedTitles = ref([])
const knowledgeList = ref([])
const adminLogs = ref([])
const postTotal = ref(0)
const replyTotal = ref(0)
const userTotal = ref(0)

const adminFilters = reactive({ keyword: '', status: null })
const postFilters = reactive({ keyword: '', status: null, visibility: null })
const replyFilters = reactive({ keyword: '', postId: '', status: null, visibility: null })
const userFilters = reactive({ keyword: '', role: null, status: null })
const knowledgeFilters = reactive({ keyword: '', status: null, category: '' })
const logFilters = reactive({ targetType: null, operationType: '' })

const adminCreateForm = reactive({ username: '', password: '', nickname: '' })
const grantTitleForm = reactive({ username: '', titleId: null, wearing: true, remark: '' })
const knowledgeForm = reactive({ questionText: '', answerText: '', category: '', rewardPoints: '' })

const isAdmin = computed(() => [8, 9].includes(currentUser.value?.role))
const isSuperAdmin = computed(() => currentUser.value?.role === 9)
const adminRoleLabel = computed(() => (isSuperAdmin.value ? '超级管理员权限' : '管理员权限'))

function roleLabel(role) {
  if (role === 9) return '超级管理员'
  if (role === 8) return '管理员'
  if (role === 2) return '老生'
  if (role === 1) return '新生'
  return '用户'
}

function postStatusLabel(status) {
  if (status === 0) return '审核中'
  if (status === 1) return '已发布'
  if (status === 2) return '已驳回'
  if (status === 3) return '已隐藏'
  return '未知状态'
}

function knowledgeStatusLabel(status) {
  if (status === 1) return '待向量化'
  if (status === 2) return '已启用'
  if (status === 4) return '已下线'
  return '其他状态'
}

function formatTime(value) {
  if (!value) return '刚刚'
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function targetTypeLabel(targetType) {
  if (targetType === 1) return '用户'
  if (targetType === 2) return '帖子'
  if (targetType === 3) return '评论'
  if (targetType === 4) return '知识库'
  if (targetType === 6) return '头衔'
  return '其他'
}

function canBanUser(user) {
  if (!user) return false
  if (user.role === 9) return false
  if (isSuperAdmin.value) return true
  return isAdmin.value && user.role < 8
}

function goToPost(postId) {
  router.push(`/forum/${postId}`)
}

async function loadAdminAccounts() {
  if (!isSuperAdmin.value) return
  const adminPage = await fetchAdminUsers({
    pageNum: 1,
    pageSize: LIST_FETCH_SIZE,
    role: 8,
    status: adminFilters.status,
    keyword: adminFilters.keyword || undefined
  })
  const superAdminPage = await fetchAdminUsers({
    pageNum: 1,
    pageSize: LIST_FETCH_SIZE,
    role: 9,
    status: adminFilters.status,
    keyword: adminFilters.keyword || undefined
  })
  adminAccounts.value = [...(superAdminPage.records || []), ...(adminPage.records || [])]
}

async function loadAdminPosts() {
  const page = await fetchAdminPosts({
    pageNum: 1,
    pageSize: LIST_FETCH_SIZE,
    keyword: postFilters.keyword || undefined,
    status: postFilters.status,
    visibility: postFilters.visibility
  })
  adminPosts.value = page.records || []
  postTotal.value = page.total ?? adminPosts.value.length
}

async function loadAdminReplies() {
  const page = await fetchAdminReplies({
    pageNum: 1,
    pageSize: LIST_FETCH_SIZE,
    keyword: replyFilters.keyword || undefined,
    postId: replyFilters.postId ? Number(replyFilters.postId) : undefined,
    status: replyFilters.status,
    visibility: replyFilters.visibility
  })
  adminReplies.value = page.records || []
  replyTotal.value = page.total ?? adminReplies.value.length
}

async function loadUserList() {
  const page = await fetchAdminUsers({
    pageNum: 1,
    pageSize: LIST_FETCH_SIZE,
    keyword: userFilters.keyword || undefined,
    role: userFilters.role,
    status: userFilters.status
  })
  siteUsers.value = page.records || []
  userTotal.value = page.total ?? siteUsers.value.length
}

async function loadTitleOptions() {
  titleOptions.value = await fetchTitles()
}

async function loadGrantedTitles() {
  const page = await fetchGrantedTitles({ pageNum: 1, pageSize: LIST_FETCH_SIZE })
  grantedTitles.value = page.records || []
}

async function loadKnowledgeList() {
  const page = await fetchKnowledgeList({
    pageNum: 1,
    pageSize: LIST_FETCH_SIZE,
    keyword: knowledgeFilters.keyword || undefined,
    status: knowledgeFilters.status,
    category: knowledgeFilters.category || undefined
  })
  knowledgeList.value = page.records || []
}

async function loadAdminLogs() {
  const page = await fetchAdminLogs({
    pageNum: 1,
    pageSize: LIST_FETCH_SIZE,
    targetType: logFilters.targetType,
    operationType: logFilters.operationType || undefined
  })
  adminLogs.value = page.records || []
}

async function reloadCurrentTab() {
  if (!isAdmin.value) return
  loading.any = true
  try {
    if (activeTab.value === 'admins' && isSuperAdmin.value) {
      await loadAdminAccounts()
    } else if (activeTab.value === 'posts') {
      await loadAdminPosts()
    } else if (activeTab.value === 'replies') {
      await loadAdminReplies()
    } else if (activeTab.value === 'users') {
      await loadUserList()
    } else if (activeTab.value === 'ops') {
      await Promise.all([loadTitleOptions(), loadGrantedTitles(), loadKnowledgeList(), loadAdminLogs()])
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.any = false
  }
}

async function submitCreateAdmin() {
  if (!adminCreateForm.username.trim() || !adminCreateForm.password.trim() || !adminCreateForm.nickname.trim()) {
    ElMessage.warning('请完整填写管理员账号信息')
    return
  }
  loading.createAdmin = true
  try {
    await createAdminUser({
      username: adminCreateForm.username.trim(),
      password: adminCreateForm.password.trim(),
      nickname: adminCreateForm.nickname.trim()
    })
    adminCreateForm.username = ''
    adminCreateForm.password = ''
    adminCreateForm.nickname = ''
    ElMessage.success('管理员账号已创建')
    await loadAdminAccounts()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.createAdmin = false
  }
}

async function toggleAdminStatus(user) {
  try {
    await updateAdminUserStatus(user.id, user.status === 1 ? 0 : 1)
    ElMessage.success(user.status === 1 ? '管理员账号已禁用' : '管理员账号已启用')
    await loadAdminAccounts()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function togglePostVisibility(post) {
  try {
    const { value } = await ElMessageBox.prompt(
      post.visibility === 1 ? `确认隐藏帖子《${post.title}》？` : `确认恢复帖子《${post.title}》为可见？`,
      post.visibility === 1 ? '隐藏帖子' : '恢复帖子',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputPlaceholder: '操作原因（可选）'
      }
    )
    await updateAdminPostVisibility(post.id, {
      visible: post.visibility !== 1,
      reason: value || (post.visibility === 1 ? '后台隐藏帖子' : '后台恢复帖子')
    })
    ElMessage.success(post.visibility === 1 ? '帖子已隐藏' : '帖子已恢复可见')
    await loadAdminPosts()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || '操作失败')
  }
}

async function confirmDeletePost(post) {
  try {
    const { value } = await ElMessageBox.prompt(`确认删除帖子《${post.title}》？可填写删除原因。`, '删除帖子', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      inputPlaceholder: '删除原因（可选）'
    })
    await deleteAdminPost(post.id, { reason: value || '后台手动删除帖子' })
    ElMessage.success('帖子已删除')
    await loadAdminPosts()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || '删除失败')
  }
}

async function toggleReplyVisibility(reply) {
  try {
    const { value } = await ElMessageBox.prompt(
      reply.visibility === 1 ? '确认隐藏该评论？' : '确认恢复该评论为可见？',
      reply.visibility === 1 ? '隐藏评论' : '恢复评论',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputPlaceholder: '操作原因（可选）'
      }
    )
    await updateAdminReplyVisibility(reply.id, {
      visible: reply.visibility !== 1,
      reason: value || (reply.visibility === 1 ? '后台隐藏评论' : '后台恢复评论')
    })
    ElMessage.success(reply.visibility === 1 ? '评论已隐藏' : '评论已恢复可见')
    await loadAdminReplies()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || '操作失败')
  }
}

async function confirmDeleteReply(reply) {
  try {
    const { value } = await ElMessageBox.prompt('确认删除该评论？可填写删除原因。', '删除评论', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      inputPlaceholder: '删除原因（可选）'
    })
    await deleteAdminReply(reply.id, { reason: value || '后台手动删除评论' })
    ElMessage.success('评论已删除')
    await loadAdminReplies()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || '删除失败')
  }
}

async function openBanPrompt(user) {
  try {
    const { value } = await ElMessageBox.prompt(`请输入封禁用户 ${user.nickname} 的原因`, '封禁用户', {
      confirmButtonText: '确认封禁',
      cancelButtonText: '取消',
      inputPlaceholder: '封禁原因'
    })
    await banUser(user.id, { reason: value || '后台手动封禁', banExpireAt: null })
    ElMessage.success('用户已封禁')
    await loadUserList()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || '封禁失败')
  }
}

async function handleUnbanUser(user) {
  try {
    await unbanUser(user.id)
    ElMessage.success('用户已解封')
    await loadUserList()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function submitGrantTitle() {
  const username = String(grantTitleForm.username || '').trim()
  if (!username || !grantTitleForm.titleId) {
    ElMessage.warning('请填写账号并选择头衔')
    return
  }
  try {
    await grantTitle({
      username,
      titleId: grantTitleForm.titleId,
      wearing: grantTitleForm.wearing,
      remark: grantTitleForm.remark
    })
    ElMessage.success('头衔已授予')
    grantTitleForm.username = ''
    grantTitleForm.titleId = null
    grantTitleForm.wearing = true
    grantTitleForm.remark = ''
    await Promise.all([loadGrantedTitles(), loadAdminLogs()])
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function revokeGrantedTitle(grant) {
  try {
    const { value } = await ElMessageBox.prompt(`确认撤销 ${grant.userNickname} 的头衔「${grant.titleName}」？`, '撤销头衔', {
      confirmButtonText: '确认撤销',
      cancelButtonText: '取消',
      inputPlaceholder: '撤销原因（可选）'
    })
    await revokeTitle(grant.id, { reason: value || '后台撤销头衔' })
    ElMessage.success('头衔已撤销')
    await Promise.all([loadGrantedTitles(), loadAdminLogs()])
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message)
  }
}

async function submitKnowledge() {
  if (!knowledgeForm.questionText.trim() || !knowledgeForm.answerText.trim()) {
    ElMessage.warning('请填写知识库问题和答案')
    return
  }
  try {
    await createKnowledge({
      questionText: knowledgeForm.questionText.trim(),
      answerText: knowledgeForm.answerText.trim(),
      category: knowledgeForm.category.trim() || null,
      rewardPoints: knowledgeForm.rewardPoints ? Number(knowledgeForm.rewardPoints) : 0
    })
    ElMessage.success('知识库条目已创建')
    knowledgeForm.questionText = ''
    knowledgeForm.answerText = ''
    knowledgeForm.category = ''
    knowledgeForm.rewardPoints = ''
    await Promise.all([loadKnowledgeList(), loadAdminLogs()])
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function changeKnowledgeStatus(item, status) {
  try {
    const { value } = await ElMessageBox.prompt(
      `确认将知识条目改为「${knowledgeStatusLabel(status)}」？`,
      '更新知识条目状态',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputPlaceholder: '变更原因（可选）'
      }
    )
    await updateKnowledgeStatus(item.id, {
      status,
      reason: value || `后台更新为${knowledgeStatusLabel(status)}`
    })
    ElMessage.success('知识条目状态已更新')
    await Promise.all([loadKnowledgeList(), loadAdminLogs()])
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message)
  }
}

onMounted(async () => {
  await ensureShellReady()
  if (isAdmin.value) {
    await reloadCurrentTab()
  }
})

watch(
  () => activeTab.value,
  async () => {
    if (isAdmin.value) {
      await reloadCurrentTab()
    }
  }
)

watch(
  () => currentUser.value?.id,
  async (nextId) => {
    if (nextId && isAdmin.value) {
      await reloadCurrentTab()
      return
    }
    adminAccounts.value = []
    adminPosts.value = []
    adminReplies.value = []
    siteUsers.value = []
    titleOptions.value = []
    grantedTitles.value = []
    knowledgeList.value = []
    adminLogs.value = []
    postTotal.value = 0
    replyTotal.value = 0
    userTotal.value = 0
  }
)
</script>

<style scoped>
:deep(.admin-tabs .el-tabs__item.is-active) {
  color: #8b261e;
}

:deep(.admin-tabs .el-tabs__active-bar) {
  background-color: #8b261e;
}
</style>
