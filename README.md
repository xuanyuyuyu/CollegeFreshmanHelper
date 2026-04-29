# 高校新生入学服务与交流平台

面向高校新生的入学服务、论坛交流与后续智能问答场景的全栈项目。当前已经落地的主线是 `论坛 + 个人中心 + 后台管理`，`内容审核、论坛飞轮、RAG 问答` 仍在后续迭代中。

## 1. 项目概览

### 1.1 当前定位

- 首页与导航：承接校园入口、论坛、智能问答、指南、个人中心、后台管理
- 论坛：帖子列表、帖子详情、双层展示回复、点赞、浏览、排序、筛选
- 个人中心：我的资料、我的帖子、我的回复、我的点赞、我的获赞
- 后台管理：管理员账号、帖子管理、评论管理、用户治理、运营台
- 智能问答：页面已存在，但真正可用的 RAG 链路尚未完成

### 1.2 当前实际状态

已完成：

- 注册、登录、退出、获取当前用户
- 登录/注册前端基础校验与字段级报错
- 注册补充 `confirmPassword`、`role`、`admissionYear`
- 导航、首页、论坛、我的、后台管理等主要前端页面
- 论坛发帖、帖子详情、回复、点赞、浏览数、热门排序
- 回复“多层语义、双层展示”模型
- 作者删除自己的帖子和回复
- 个人中心“我的帖子 / 我的回复 / 我的点赞 / 我的获赞”
- 头衔统一到 `sys_title + sys_user_title`
- 自动授予 / 自动佩戴头衔，且一个用户只保留一个头衔
- 后台按账号人工授予 / 撤销头衔
- 后台帖子、评论、用户、管理员、知识库、操作日志基础治理能力
- 论坛无限滚动与返回列表位置恢复
- Flyway 迁移和基础造数脚本

暂未完成：

- 文本 / 图片内容审核
- RabbitMQ 驱动的审核链路
- 论坛高价值回复自动提炼入知识库
- 真正可用的知识检索与 RAG 问答
- Qdrant 接入
- LLM / Embedding 接入
- 更严格的匿名防刷与风控

## 2. 技术栈

### 2.1 后端

- Java 17
- Spring Boot 3.4.0
- MyBatis-Plus 3.5.10.1
- Sa-Token 1.44.0 + JWT
- MySQL 8.x
- Redis
- RabbitMQ
- Flyway

### 2.2 前端

- Vue 3.5
- Vue Router 4
- Element Plus 2.10
- Tailwind CSS 3
- Axios
- Vite 6

### 2.3 规划中的外部能力

- 阿里云 OSS
- 阿里云内容安全
- 百炼 LLM / Embedding
- Qdrant

## 3. 目录结构

```text
CollegeFreshmanHelper/
├─ src/main/java/com/example/collegefreshmanhelper/
│  ├─ admin/        后台治理、知识库、头衔、操作日志
│  ├─ auth/         注册登录与登录态
│  ├─ common/       通用响应、异常、配置、分页
│  ├─ forum/        帖子、回复、点赞、浏览
│  └─ user/         个人中心、资料、统计、头衔显示
├─ src/main/resources/db/migration/
│  ├─ V1__init_schema.sql
│  ├─ V2__seed_superadmin_login.sql
│  ├─ V3__fix_user_stats_deleted_column.sql
│  ├─ V4__add_forum_like_record.sql
│  └─ V5__enforce_single_user_title.sql
├─ frontend/src/
│  ├─ api/          前端接口封装
│  ├─ layouts/      主布局与登录注册弹窗
│  ├─ pages/        首页、论坛、详情、我的、后台等页面
│  ├─ router/       前端路由
│  ├─ stores/       appShell 状态
│  └─ utils/        头衔颜色等工具
├─ docs/
│  ├─ CHAT_CONTEXT.md
│  └─ DEVELOPMENT.md
└─ scripts/         本地辅助脚本与造数脚本
```

## 4. 核心业务能力

### 4.1 认证与用户

- 注册、登录、退出、当前用户信息
- 用户名文案已统一成“账号”
- 注册支持身份选择：`我是新生 / 我是老生`
- 注册支持可选入学年份，默认值为 `2026`
- 可修改昵称、头像 URL、身份、入学年份

### 4.2 论坛

- 帖子列表分页接口
- 前端长列表无限滚动加载
- 分类筛选：`宿舍 / 学习 / 食堂 / 军训 / 其他`
- 排序：`最新发布 / 热门优先`
- 帖子详情、图片展示、浏览计数
- 一级回复和楼中楼展示
- 帖子点赞 / 取消点赞
- 回复点赞 / 取消点赞
- 作者删除自己的帖子
- 作者删除自己的回复
- 从详情返回论坛时恢复原列表位置

### 4.3 个人中心

- 我的资料
- 我的帖子
- 我的回复
- 我的点赞
- 我的获赞
- 头衔展示与规则说明

### 4.4 后台管理

- 管理员账号管理
- 帖子管理
- 评论管理
- 用户治理
- 运营台
- 知识库管理
- 头衔管理
- 操作日志查询

说明：

- 奖励申请整条业务线已经删除，不要再恢复
- 内容审核明确放到最后做

## 5. 核心设计约束

### 5.1 回复模型

当前采用“多层语义、双层展示”：

- 一级回复：`parent_id = 0`
- 楼中楼回复：`parent_id = 所属一级回复ID`
- 实际回复目标：`reply_to_reply_id`
- 实际回复用户：`reply_to_user_id`

这样既支持回复任意一条回复，也避免前端无限嵌套。

### 5.2 点赞模型

- `forum_like_record` 持久化点赞关系与点赞时间
- Redis 保存点赞关系和计数缓存
- 定时任务回刷最终计数到 MySQL
- “我的点赞”按持久化点赞记录查询

### 5.3 浏览数模型

- 当前仅对登录用户做去重
- 维度：`postId + userId`
- TTL：12 小时

### 5.4 头衔模型

- 头衔统一来自 `sys_title`
- 用户实际佩戴状态统一来自 `sys_user_title`
- 一个用户只能有一条 `sys_user_title`
- 管理员手动授予优先级最高
- 没有管理员头衔时，系统根据规则自动授予 / 自动切换默认头衔

## 6. 头衔现状

当前前后端统一使用 `sys_title`，默认头衔不再前端硬编码。

当前展示颜色规则：

- `热心答主`：蓝色
- `高赞答主`：蓝色
- `知识共建者`：蓝色
- `校园百晓生`：紫色
- `官方认证学长`：金色

说明：

- “默认头衔”和 `sys_title` 已统一
- 论坛列表、帖子详情、回复区、个人中心都走同一套头衔显示逻辑

## 7. 数据库与迁移

### 7.1 迁移版本

- [V1__init_schema.sql](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/resources/db/migration/V1__init_schema.sql)
- [V2__seed_superadmin_login.sql](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/resources/db/migration/V2__seed_superadmin_login.sql)
- [V3__fix_user_stats_deleted_column.sql](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/resources/db/migration/V3__fix_user_stats_deleted_column.sql)
- [V4__add_forum_like_record.sql](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/resources/db/migration/V4__add_forum_like_record.sql)
- [V5__enforce_single_user_title.sql](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/resources/db/migration/V5__enforce_single_user_title.sql)

### 7.2 核心表

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

### 7.3 已知说明

- `knowledge_qa_trace` 已预留 `contributor_user_id`、`source_reply_id`
- 论坛飞轮真正接入后，需要在“回复提炼进知识库”时补齐这两个字段
- 本地库已经造过较多论坛测试数据，便于联调无限滚动和互动数据

### 7.4 Flyway 注意事项

初始化 SQL 之前已经改过，且移除了旧的奖励申请相关内容。  
如果本地数据库执行过旧迁移版本，建议：

1. 重建开发库
2. 或先执行 Flyway `repair`

### 7.5 默认超级管理员

- 账号：`superadmin`
- 密码：`Admin@123456`

上线前必须修改默认密码。

## 8. 接口概览

### 8.1 认证

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`

### 8.2 论坛

- `POST /api/posts`
- `GET /api/posts/page`
- `GET /api/posts/{postId}`
- `DELETE /api/posts/{postId}`
- `POST /api/posts/{postId}/like`
- `DELETE /api/posts/{postId}/like`
- `POST /api/posts/{postId}/replies`
- `GET /api/posts/{postId}/replies`
- `DELETE /api/posts/{postId}/replies/{replyId}`
- `POST /api/replies/{replyId}/like`
- `DELETE /api/replies/{replyId}/like`

### 8.3 个人中心

- `GET /api/users/me/summary`
- `GET /api/users/me/posts`
- `GET /api/users/me/replies`
- `GET /api/users/me/liked-items`
- `GET /api/users/me/likes`
- `PUT /api/users/me/profile`
- `PUT /api/users/me/avatar`

### 8.4 后台

- 管理员账号管理接口
- 帖子 / 评论治理接口
- 用户治理接口
- 头衔授予 / 撤销 / 列表接口
- 知识库管理接口
- 操作日志接口

## 9. 本地运行

### 9.1 后端

```bash
mvn spring-boot:run
```

### 9.2 前端

```bash
cd frontend
npm install
npm run dev
```

### 9.3 编译检查

```bash
mvn -q -DskipTests compile
cd frontend && npm run build
```

## 10. 当前最值得继续开发的方向

1. 论坛回复提炼入知识库的飞轮链路
2. 内容审核接入
3. 知识库检索与 RAG 问答闭环
4. 论坛长列表进一步优化
5. 更细的风控、防刷和运营策略
