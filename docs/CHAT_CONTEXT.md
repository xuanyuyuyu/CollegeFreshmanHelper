# 上下文交接文档

本文档用于在新聊天窗口中快速恢复当前项目上下文。  
默认工作目录：

- `/Users/xuan/IdeaProjects/CollegeFreshmanHelper`

## 1. 项目当前状态

### 1.1 技术结构

- 后端：Spring Boot 3.4 + Java 17 + MyBatis-Plus + Sa-Token + MySQL + Redis + RabbitMQ + Flyway
- 前端：Vite + Vue 3 + Vue Router + Element Plus + Tailwind

### 1.2 当前主线

已经比较完整可用：

- 论坛
- 个人中心
- 后台管理

尚未真正落地：

- 内容审核
- 论坛高价值回复提炼入知识库
- Qdrant
- Embedding / LLM
- RAG 问答闭环

## 2. 用户明确要求与长期偏好

### 2.1 产品方向

- 当前论坛、个人中心、后台管理已存在，不要重做
- 奖励申请逻辑已经删除，不要恢复
- 内容审核放最后做
- 论坛风格偏“贴吧感”
- 回复要支持多层语义，但前端保持双层展示
- 后台要做成真实可操作，而不是空壳

### 2.2 开发风格

- 更偏向“直接做”，而不是长篇方案
- 希望前后端联动，能跑通主链路
- 文档必须跟当前代码现状一致
- 修改代码时，优先延续现有结构，不大拆

### 2.3 已明确的业务规则

- 一个用户只能拥有一个头衔
- 一个用户同时只能佩戴这一个头衔
- 管理员手动授予头衔优先级最高
- 注册时“账号”是前端文案，后端字段仍是 `username`
- 注册默认入学年份为 `2026`

## 3. 最近已完成的重要事项

### 3.1 认证与注册登录体验

- 登录、注册、退出、当前用户已完成
- 登录 / 注册前端增加基础校验
- 注册增加 `confirmPassword`
- 注册增加 `role`
- 注册增加 `admissionYear`
- 注册成功后不再回填明文密码到登录框
- 用户名文案改成“账号”
- 支持回车提交

### 3.2 论坛

- 帖子列表、详情、发帖、回复已通
- 帖子点赞、回复点赞已通
- 浏览数已实现
- 热门排序已实现
- 分类筛选已改成真实后端筛选
- 帖子作者可以删除自己的帖子
- 回复作者可以删除自己的回复
- 删除一级回复会级联删除其子回复
- 删除二级回复只删自身，并维护相关计数
- 论坛前端已改成无限滚动
- 从详情返回论坛时，恢复离开前的列表状态和滚动位置

### 3.3 个人中心

- 我的资料
- 我的帖子
- 我的回复
- 我的点赞
- 我的获赞
- 头衔展示

### 3.4 后台管理

已存在并联通：

- 管理员账号管理
- 帖子管理
- 评论管理
- 用户治理
- 运营台
- 知识库管理
- 头衔管理
- 操作日志

### 3.5 头衔系统

- 默认头衔和 `sys_title` 已统一
- 论坛、回复、我的页面都统一读当前佩戴头衔
- 管理员人工授予时改成输入“账号”而不是 `userId`
- 自动授予 / 自动佩戴逻辑已实现
- `sys_user_title` 已通过迁移收口成“一个用户只保留一条记录”
- 管理员撤销头衔后，会自动回退到符合规则的自动头衔

### 3.6 统计与知识贡献

- `knowledgeContributionCount`、`featuredAnswerCount` 已接入 `user_stats`
- 后台知识库新增或状态变更后，会同步回写用户统计并重算头衔
- `knowledge_qa_trace` 已有 `contributor_user_id`、`source_reply_id` 字段

## 4. 已明确但暂未实现的事项

### 4.1 飞轮相关

用户已经明确记住以下两点，等飞轮实现后补：

1. 论坛回复被提炼进知识库时，自动写 `contributor_user_id`
2. 如果是从回复提炼来的，再写 `source_reply_id`

### 4.2 审核相关

- 文本审核未做
- 图片审核未做
- MQ 驱动审核链路未做

## 5. 当前后台权限规则

### 超级管理员

- 创建普通管理员账号
- 启用 / 禁用普通管理员账号
- 拥有更高等级后台操作权限

### 普通管理员

- 删帖
- 删评
- 隐藏 / 恢复帖子
- 隐藏 / 恢复评论
- 封禁普通用户
- 解封普通用户

### 特殊约束

- 超级管理员不可被封禁
- 普通管理员不能处理管理员账号

## 6. 当前数据库与数据状态

### 6.1 Flyway 迁移

- `V1__init_schema.sql`
- `V2__seed_superadmin_login.sql`
- `V3__fix_user_stats_deleted_column.sql`
- `V4__add_forum_like_record.sql`
- `V5__enforce_single_user_title.sql`

### 6.2 关键表

- `sys_user`
- `user_stats`
- `sys_title`
- `sys_user_title`
- `forum_post`
- `forum_post_image`
- `forum_reply`
- `forum_like_record`
- `knowledge_qa_trace`
- `admin_operation_log`

### 6.3 本地造数情况

本地开发库已经造过一批论坛数据，量级大致为：

- 帖子约 68 条
- 回复约 223 条
- 点赞记录约 670 条

用于联调无限滚动、我的点赞、热门排序和删除逻辑。

## 7. 当前重要文件

### 7.1 后端核心

- [AuthController.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/auth/controller/AuthController.java)
- [ForumPostController.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/forum/controller/ForumPostController.java)
- [ForumReplyController.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/forum/controller/ForumReplyController.java)
- [ForumPostServiceImpl.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/forum/service/impl/ForumPostServiceImpl.java)
- [ForumReplyServiceImpl.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/forum/service/impl/ForumReplyServiceImpl.java)
- [MyCenterServiceImpl.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/user/service/impl/MyCenterServiceImpl.java)
- [UserTitleDisplayService.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/user/service/UserTitleDisplayService.java)
- [UserStatsSyncServiceImpl.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/user/service/impl/UserStatsSyncServiceImpl.java)
- [AdminContentServiceImpl.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/admin/service/impl/AdminContentServiceImpl.java)
- [AdminTitleServiceImpl.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/admin/service/impl/AdminTitleServiceImpl.java)
- [AdminKnowledgeServiceImpl.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/admin/service/impl/AdminKnowledgeServiceImpl.java)

### 7.2 前端核心

- [MainLayout.vue](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/layouts/MainLayout.vue)
- [ForumPage.vue](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/pages/ForumPage.vue)
- [ForumDetailPage.vue](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/pages/ForumDetailPage.vue)
- [MePage.vue](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/pages/MePage.vue)
- [AdminPage.vue](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/pages/AdminPage.vue)
- [appShell.js](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/stores/appShell.js)
- [forum.js](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/api/forum.js)
- [me.js](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/api/me.js)
- [admin.js](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/api/admin.js)
- [titleBadge.js](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/utils/titleBadge.js)

## 8. 新窗口起手提示

如果在新窗口继续开发，建议直接告诉模型：

1. 先阅读 [docs/CHAT_CONTEXT.md](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/docs/CHAT_CONTEXT.md)
2. 再阅读 [README.md](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/README.md) 和 [docs/DEVELOPMENT.md](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/docs/DEVELOPMENT.md)
3. 当前论坛、个人中心、后台管理已存在，不要重做
4. 奖励申请逻辑已经删除，不要再恢复
5. 内容审核放最后做
6. 若做论坛飞轮，记得补 `contributor_user_id` 和 `source_reply_id`
