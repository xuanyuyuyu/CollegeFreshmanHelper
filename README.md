# 高校新生入学服务与交流平台

面向高校新生的入学服务、交流与智能问答平台。项目当前以 `论坛 + 个人中心 + 管理后台` 为主要已落地主线，`RAG 问答闭环、内容审核、Qdrant 接入` 仍属于后续迭代重点。

## 1. 项目概览

### 1.1 业务定位

- `Forum`：新生与老生围绕宿舍、学习、军训、食堂等话题进行发帖、回帖、互动
- `AI Assistant`：后续基于提纯 QA 知识库提供智能问答
- `Admin Console`：管理员和超级管理员进行内容治理、账号治理、知识库维护和日志追踪

### 1.2 当前实际状态

当前项目已经完成：

- 用户注册、登录、退出、获取当前用户
- 论坛主链路：发帖、列表、详情、多层回复语义、点赞、浏览数、热门排序
- 个人中心：我的资料、我的帖子、我的回复、我的获赞、头衔展示
- 管理后台：帖子管理、评论管理、用户治理、管理员账号管理、头衔管理、知识库管理、操作日志
- 前端多页面与后端接口联动

当前项目尚未完成：

- 文本/图片内容审核
- 图片异步审核 MQ 链路
- Qdrant 接入
- 百炼 LLM / Embedding 接入
- 高赞回复自动提纯入库
- 真正可用的 RAG 问答接口
- 匿名浏览防刷

## 2. 技术栈

### 2.1 后端

- Java 17
- Spring Boot 3.4.x
- MyBatis-Plus
- Sa-Token + JWT
- MySQL 8.x
- Redis
- RabbitMQ
- Flyway

### 2.2 前端

- Vue 3
- Vue Router
- Element Plus
- Tailwind CSS
- Axios
- Vite

### 2.3 规划中的外部能力

- 阿里云 OSS
- 阿里云 Green
- 阿里百炼
- Qdrant

## 3. 资源约束

项目目标部署环境仍然是 `2C2G`：

- JVM：`-Xms256m -Xmx512m`
- MySQL：建议 `innodb_buffer_pool_size=128M`
- Qdrant：建议 `mem_limit: 300M`，并使用 on-disk 模式

## 4. 当前已实现模块

### 4.1 认证与用户

- 注册
- 登录
- 获取当前用户信息
- 退出登录
- 修改个人资料
- 修改头像 URL
- 超级管理员初始化账号

### 4.2 论坛

- 发帖
- 帖子分页列表
- 帖子详情
- 多层回复语义支持
- 双层展示结构
- 帖子点赞 / 取消点赞
- 回复点赞 / 取消点赞
- 浏览数统计
- 已登录用户 12 小时内浏览去重
- 最新 / 热门排序

### 4.3 个人中心

- 我的概览
- 我的帖子
- 我的回复
- 我的获赞统计
- 答疑头衔展示

说明：

- 项目中已经移除了“申请奖励”相关逻辑
- 当前保留的是 `头衔、积分、知识贡献` 体系

### 4.4 管理后台

#### 管理员账号

- 超级管理员创建普通管理员账号
- 超级管理员启用 / 禁用普通管理员账号
- 管理员账号分页与关键词筛选

#### 帖子管理

- 帖子分页
- 按标题 / 正文 / 作者 / 帖子 ID 搜索
- 按状态 / 可见性筛选
- 隐藏帖子
- 恢复帖子可见
- 删除帖子

#### 评论管理

- 评论分页
- 按评论内容 / 作者 / 被回复用户 / 来源帖子标题正文 / 帖子 ID 搜索
- 按状态 / 可见性筛选
- 隐藏评论
- 恢复评论可见
- 删除评论

#### 用户治理

- 用户分页
- 按账号 / 昵称 / 用户 ID 搜索
- 按角色 / 状态筛选
- 管理员可封禁普通用户
- 管理员可解封普通用户
- 超级管理员保留更高权限
- 超级管理员不可被封禁

#### 其他后台能力

- 用户详情查看
- 操作日志分页与筛选
- 手动新增知识库条目
- 知识库条目分页、筛选、上下线
- 头衔列表查询
- 人工授予头衔
- 已授予头衔分页查询
- 人工撤销头衔

## 5. 核心设计约束

### 5.1 回复模型

回复采用“多层语义、双层展示”设计：

- 一级回复：`parent_id = 0`
- 所有楼中楼回复：`parent_id = 所属一级回复ID`
- 实际回复目标：`reply_to_reply_id`
- 实际回复用户：`reply_to_user_id`

这样可以支持：

- A 回复主楼
- B 回复 A
- C 回复 “B 对 A 的回复”

但前端依然固定为两层展示，避免无限缩进。

### 5.2 点赞模型

- Redis Set 存用户点赞关系
- Redis Hash 暂存最新点赞数
- 定时任务批量回刷 MySQL
- 不允许每次点赞直接 `UPDATE` MySQL 计数

### 5.3 浏览数模型

- 当前只对登录用户做去重
- Redis Key 按 `postId + userId` 隔离
- TTL 12 小时

## 6. 头衔规则

当前系统保留答疑头衔逻辑：

- `热心答主`：回复数 `>= 20` 或累计获赞 `>= 30`
- `知识共建者`：知识贡献数 `>= 3` 或高质量回答数 `>= 2`
- `高赞答主`：累计获赞 `>= 80` 或高质量回答数 `>= 5`

未达到条件前默认不展示头衔。

## 7. 数据库与迁移

项目当前使用 Flyway 管理初始化脚本：

- [V1__init_schema.sql](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/resources/db/migration/V1__init_schema.sql)
- [V2__seed_superadmin_login.sql](/Users/xuan/IdeaProjects/CollegeFreshmanHelper/src/main/resources/db/migration/V2__seed_superadmin_login.sql)

### 7.1 核心表

- `sys_user`
- `user_stats`
- `sys_title`
- `sys_user_title`
- `forum_post`
- `forum_post_image`
- `forum_reply`
- `knowledge_qa_trace`
- `admin_operation_log`

### 7.2 重要说明

最近已经修改过初始化 SQL，移除了“奖励申请”整条业务线。  
如果你的本地数据库已经执行过旧版 Flyway 迁移，重新启动时可能遇到 checksum 差异。

开发环境建议：

1. 直接重建数据库重新初始化
2. 或先做 Flyway `repair` 再继续

### 7.3 超级管理员

- 用户名：`superadmin`
- 密码：`Admin@123456`

上线前必须修改默认密码。

## 8. 后端接口概览

### 8.1 认证

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`

### 8.2 论坛

- `POST /api/posts`
- `GET /api/posts/page`
- `GET /api/posts/{postId}`
- `POST /api/posts/{postId}/like`
- `DELETE /api/posts/{postId}/like`
- `POST /api/posts/{postId}/replies`
- `GET /api/posts/{postId}/replies`
- `POST /api/posts/{replyId}/like`
- `DELETE /api/posts/{replyId}/like`

### 8.3 个人中心

- `GET /api/users/me/summary`
- `GET /api/users/me/posts`
- `GET /api/users/me/replies`
- `GET /api/users/me/likes`
- `PUT /api/users/me/profile`
- `PUT /api/users/me/avatar`

### 8.4 管理后台

- `GET /api/admin/users/page`
- `GET /api/admin/users/{userId}`
- `POST /api/admin/users/{userId}/ban`
- `POST /api/admin/users/{userId}/unban`
- `POST /api/admin/users/admins`
- `POST /api/admin/users/{userId}/status`
- `GET /api/admin/posts/page`
- `POST /api/admin/posts/{postId}/visibility`
- `POST /api/admin/posts/{postId}/delete`
- `GET /api/admin/replies/page`
- `POST /api/admin/replies/{replyId}/visibility`
- `POST /api/admin/replies/{replyId}/delete`
- `GET /api/admin/knowledge/page`
- `POST /api/admin/knowledge`
- `POST /api/admin/knowledge/{knowledgeId}/status`
- `GET /api/admin/titles`
- `GET /api/admin/titles/grants/page`
- `POST /api/admin/titles/grant`
- `POST /api/admin/titles/grants/{userTitleId}/revoke`
- `GET /api/admin/logs/page`

## 9. 包结构

当前后端以业务域拆分：

```text
src/main/java/com/example/collegefreshmanhelper
├── admin
├── auth
├── common
├── forum
└── user
```

后续建议新增：

- `ai`
- `knowledge`
- `mq`
- `integration`

## 10. 本地运行

### 10.1 环境要求

- JDK 17
- Maven 3.9+
- MySQL 8.x
- Redis 6.x / 7.x
- Node.js 18+
- npm 9+

### 10.2 后端启动

```bash
mvn spring-boot:run
```

默认端口：`8080`

### 10.3 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 10.4 编译检查

```bash
mvn -q -DskipTests compile
cd frontend && npm run build
```

## 11. 下一步建议

建议优先级：

1. 内容审核接入
2. MQ 驱动的高赞回复提纯链路
3. Qdrant + Embedding + 百炼问答接口
4. 我的获赞明细
5. 匿名浏览防刷
