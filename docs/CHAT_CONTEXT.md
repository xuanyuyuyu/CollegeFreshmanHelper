# 上下文交接文档

本文档用于在新聊天窗口中快速恢复项目上下文。

## 1. 项目当前状态

项目目录：

- `/Users/xuan/IdeaProjects/CollegeFreshmanHelper`

项目类型：

- 后端：Spring Boot 3 + Java 17
- 前端：Vite + Vue 3 + Element Plus + Tailwind

当前主线：

- 论坛系统已基本可用
- 个人中心已可用
- 后台管理已基本可用
- RAG、内容审核、向量库链路尚未真正落地

## 2. 用户明确要求与长期偏好

### 技术方向

- 极度重视 `2C2G` 场景下的资源约束
- 偏向轻量、实用、可跑通
- 喜欢先把主链路做通，再补外围能力

### 产品偏好

- 论坛风格偏向“贴吧感”
- 回复要支持多层语义，但前端保持双层展示
- 后台管理需要真实可操作，不要只有空页面
- 内容审核明确要求放到最后做

### 沟通偏好

- 希望直接做，不喜欢只停留在分析
- 需要随时能落地成前后端联动
- 文档要和代码现状一致

## 3. 最近已完成的重要事项

### 论坛

- 帖子详情、帖子列表、发帖、回帖已通
- 帖子点赞、评论点赞已通
- 浏览数已实现
- 登录用户 12 小时内重复浏览同帖不重复计数
- 热门排序已实现

### 多层回复

- 支持“用户 3 回复 用户 1 回复 用户 2 的那条回复”
- 数据层支持多层语义
- 展示层仍固定双层结构

### 个人中心

- 我的资料、我的帖子、我的回复、我的获赞已实现
- 头衔规则已接入
- 默认不展示头衔，达标后才显示

### 后台管理

已新增独立 [后台管理] 页面，并挂到顶部导航：

- 管理员账号管理
- 帖子管理
- 评论管理
- 用户治理
- 头衔管理
- 知识库管理
- 操作日志

### 搜索能力

后台管理搜索能力已经补齐：

- 帖子：标题 / 正文 / 作者 / 帖子 ID
- 用户：账号 / 昵称 / 用户 ID
- 评论：评论内容 / 作者 / 被回复用户 / 来源帖子 / 帖子 ID

### 奖励申请

这条业务线已经被彻底移除：

- 奖励申请表逻辑删除
- 奖励申请审核接口删除
- 个人中心奖励资格文案删除
- 后台奖励审核模块删除

保留的是：

- 头衔
- 积分
- 知识贡献

## 4. 当前后台权限规则

### 超级管理员

- 创建普通管理员账号
- 启用 / 禁用普通管理员账号
- 可处理普通用户和管理员之外的更高权限后台动作

### 普通管理员

- 可删帖
- 可删评
- 可隐藏 / 恢复帖子
- 可隐藏 / 恢复评论
- 可封禁普通用户
- 不可处理管理员账号

### 特殊约束

- 超级管理员账号不可封禁

## 5. 当前仍未完成的关键能力

### 高优先级

- 内容审核接入
- MQ 驱动的高赞回复提纯链路
- Qdrant 接入
- 百炼 Embedding / LLM 接入
- RAG 问答接口

### 中优先级

- 我的获赞明细
- 匿名浏览防刷
- 后台更多详情弹窗 / 抽屉

## 6. 关键数据库说明

最近修改过：

- `V1__init_schema.sql`
- `V2__seed_superadmin_login.sql`

已经移除了：

- `reward_application` 相关初始化逻辑
- `user_stats` 里的奖励申请统计字段

注意：

如果本地开发数据库已经跑过旧版 Flyway 迁移，重新启动时可能出现 checksum 差异。开发环境建议直接重建数据库，或先执行 Flyway repair。

## 7. 当前重要文件

### 后端核心

- [AdminUserController.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/admin/controller/AdminUserController.java)
- [AdminContentController.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/admin/controller/AdminContentController.java)
- [AdminKnowledgeController.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/admin/controller/AdminKnowledgeController.java)
- [AdminTitleController.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/admin/controller/AdminTitleController.java)
- [AdminOperationLogController.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/admin/controller/AdminOperationLogController.java)
- [AdminContentServiceImpl.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/admin/service/impl/AdminContentServiceImpl.java)
- [AdminUserServiceImpl.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/admin/service/impl/AdminUserServiceImpl.java)
- [MyCenterServiceImpl.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/user/service/impl/MyCenterServiceImpl.java)
- [ForumPostServiceImpl.java](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/java/com/example/collegefreshmanhelper/forum/service/impl/ForumPostServiceImpl.java)

### 前端核心

- [AdminPage.vue](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/pages/AdminPage.vue)
- [ForumPage.vue](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/pages/ForumPage.vue)
- [ForumDetailPage.vue](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/pages/ForumDetailPage.vue)
- [MePage.vue](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/pages/MePage.vue)
- [MainLayout.vue](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/layouts/MainLayout.vue)
- [admin.js](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/frontend/src/api/admin.js)

## 8. 新窗口建议起手提示

如果在新窗口继续开发，建议直接告诉模型：

1. 先阅读 [docs/CHAT_CONTEXT.md](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/docs/CHAT_CONTEXT.md)
2. 再阅读 [README.md](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/README.md) 和 [docs/DEVELOPMENT.md](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/docs/DEVELOPMENT.md)
3. 当前论坛、个人中心、后台管理已存在，不要重做
4. 奖励申请逻辑已经删除，不要再恢复
5. 内容审核仍然放到最后做
