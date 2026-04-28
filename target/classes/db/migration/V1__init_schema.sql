-- ============================================
-- CollegeFreshmanHelper V2 Schema
-- MySQL 8.x
-- Charset: utf8mb4
-- Note:
-- 1. 主键默认由应用层使用 MyBatis-Plus 雪花算法生成
-- 2. 故意不加外键，降低高并发写入和异步链路耦合成本
-- 3. 点赞实时态在 Redis，MySQL 中的 like_count 为最终一致性快照
-- ============================================

CREATE DATABASE IF NOT EXISTS `college_freshman_helper`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `college_freshman_helper`;

SET NAMES utf8mb4;

-- 为了便于重复执行，先按依赖顺序删除旧表
DROP TABLE IF EXISTS `admin_operation_log`;
DROP TABLE IF EXISTS `sys_user_title`;
DROP TABLE IF EXISTS `sys_title`;
DROP TABLE IF EXISTS `user_stats`;
DROP TABLE IF EXISTS `knowledge_qa_trace`;
DROP TABLE IF EXISTS `forum_reply`;
DROP TABLE IF EXISTS `forum_post_image`;
DROP TABLE IF EXISTS `forum_post`;
DROP TABLE IF EXISTS `sys_user`;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE `sys_user` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '雪花ID',
  `username` VARCHAR(32) NOT NULL COMMENT '登录账号',
  `password_hash` VARCHAR(100) NOT NULL COMMENT 'BCrypt密码哈希',
  `nickname` VARCHAR(32) NOT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0未知 1男 2女',
  `role` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1新生 2老生 8管理员 9超级管理员',
  `admission_year` SMALLINT UNSIGNED DEFAULT NULL COMMENT '入学年份',
  `college_name` VARCHAR(64) DEFAULT NULL COMMENT '学院名称',
  `major_name` VARCHAR(64) DEFAULT NULL COMMENT '专业名称',
  `bio` VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
  `points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '社区积分',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '0禁用 1正常',
  `ban_expire_at` DATETIME DEFAULT NULL COMMENT '封禁截止时间，NULL表示未封禁',
  `ban_reason` VARCHAR(255) DEFAULT NULL COMMENT '封禁原因',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_role_status` (`role`, `status`),
  KEY `idx_sys_user_points` (`points`),
  KEY `idx_sys_user_ban_expire_at` (`ban_expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ============================================
-- 2. 用户统计表
-- 说明：避免前台频繁 count(*) 聚合
-- ============================================
CREATE TABLE `user_stats` (
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `post_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '发帖数',
  `reply_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复数',
  `reply_like_received_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复累计获赞数',
  `post_like_received_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '帖子累计获赞数',
  `knowledge_contribution_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '知识库贡献数',
  `featured_answer_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '达到高质量阈值的回答数',
  `last_calculated_at` DATETIME DEFAULT NULL COMMENT '最后统计更新时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户统计表';

-- ============================================
-- 3. 头衔定义表
-- ============================================
CREATE TABLE `sys_title` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '雪花ID',
  `title_name` VARCHAR(32) NOT NULL COMMENT '头衔名称',
  `title_code` VARCHAR(32) NOT NULL COMMENT '唯一编码',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '头衔描述',
  `icon_url` VARCHAR(255) DEFAULT NULL COMMENT '头衔图标',
  `grant_type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1自动发放 2管理员发放',
  `rule_type` TINYINT UNSIGNED DEFAULT NULL COMMENT '1回复数 2回复获赞数 3知识贡献数 4综合',
  `rule_threshold` INT UNSIGNED DEFAULT NULL COMMENT '阈值',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_title_code` (`title_code`),
  KEY `idx_sys_title_status_sort` (`status`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='头衔定义表';

-- ============================================
-- 4. 用户头衔关联表
-- ============================================
CREATE TABLE `sys_user_title` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '雪花ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `title_id` BIGINT UNSIGNED NOT NULL COMMENT '头衔ID',
  `is_wearing` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0未佩戴 1当前佩戴',
  `grant_source` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1系统自动 2管理员发放',
  `grant_remark` VARCHAR(255) DEFAULT NULL COMMENT '发放备注',
  `granted_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '发放管理员ID',
  `granted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发放时间',
  `expired_at` DATETIME DEFAULT NULL COMMENT '过期时间，可为空',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_title_user_title` (`user_id`, `title_id`),
  KEY `idx_sys_user_title_user_wearing` (`user_id`, `is_wearing`),
  KEY `idx_sys_user_title_title_id` (`title_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户头衔关联表';

-- ============================================
-- 5. 帖子表
-- 说明：
-- 1. 图文帖创建时先置 status=0(审核中)
-- 2. 纯文本帖同步审核通过后可直接 status=1
-- 3. visibility 用于后台人工隐藏/显示
-- ============================================
CREATE TABLE `forum_post` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '雪花ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '发帖用户ID',
  `title` VARCHAR(120) NOT NULL COMMENT '帖子标题',
  `content` TEXT NOT NULL COMMENT '正文内容',
  `content_type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1纯文本 2图文',
  `first_image_url` VARCHAR(255) DEFAULT NULL COMMENT '首图URL，用于列表页展示',
  `image_count` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '图片数量',
  `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
  `view_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览数',
  `reply_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复数',
  `like_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数，Redis异步回刷',
  `collect_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '0审核中 1已发布 2已驳回 3已隐藏',
  `visibility` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '0不可见 1可见',
  `audit_reason` VARCHAR(255) DEFAULT NULL COMMENT '审核驳回原因',
  `operator_admin_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '最后处理管理员ID',
  `manual_delete_reason` VARCHAR(255) DEFAULT NULL COMMENT '管理员删除/隐藏原因',
  `published_at` DATETIME DEFAULT NULL COMMENT '真正发布时间，审核通过时写入',
  `last_reply_at` DATETIME DEFAULT NULL COMMENT '最后回复时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_forum_post_user_created` (`user_id`, `created_at`),
  KEY `idx_forum_post_status_created` (`status`, `created_at`),
  KEY `idx_forum_post_status_hot` (`status`, `like_count`, `reply_count`, `created_at`),
  KEY `idx_forum_post_last_reply` (`status`, `last_reply_at`),
  KEY `idx_forum_post_visibility_deleted` (`visibility`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子表';

-- ============================================
-- 6. 帖子图片表
-- ============================================
CREATE TABLE `forum_post_image` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '雪花ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `image_url` VARCHAR(255) NOT NULL COMMENT 'OSS图片URL',
  `sort_no` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '排序号',
  `width` INT UNSIGNED DEFAULT NULL COMMENT '图片宽度',
  `height` INT UNSIGNED DEFAULT NULL COMMENT '图片高度',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2拒绝',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_forum_post_image_post_sort` (`post_id`, `sort_no`),
  KEY `idx_forum_post_image_status` (`status`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子图片表';

-- ============================================
-- 7. 双层回复表
-- 约定：
-- 1. 一级回复：parent_id = 0
-- 2. 所有楼中楼回复：parent_id = 所属一级回复ID
-- 3. reply_to_reply_id = 实际被回复的那条回复ID，可指向一级或楼中楼回复
-- 4. 因此支持“回复某条楼中楼回复”，但展示层仍保持双层结构，避免无限缩进
-- ============================================
CREATE TABLE `forum_reply` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '雪花ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '回复用户ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=一级回复；>0=所属一级回复ID（统一折叠到一级楼层）',
  `reply_to_reply_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '实际被回复的回复ID，可指向一级或楼中楼回复',
  `reply_to_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '实际被回复用户ID',
  `content` TEXT NOT NULL COMMENT '回复内容',
  `content_type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1纯文本 2图文',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '单图回复URL',
  `like_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数，Redis异步回刷',
  `child_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '一级回复下的二级回复数',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '0审核中 1已发布 2已驳回 3已隐藏',
  `visibility` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '0不可见 1可见',
  `qa_sync_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0未触发 1待提纯 2已入知识库 3失败',
  `operator_admin_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '最后处理管理员ID',
  `manual_delete_reason` VARCHAR(255) DEFAULT NULL COMMENT '管理员删除/隐藏原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_forum_reply_post_parent_created` (`post_id`, `parent_id`, `created_at`),
  KEY `idx_forum_reply_parent_created` (`parent_id`, `created_at`),
  KEY `idx_forum_reply_user_created` (`user_id`, `created_at`),
  KEY `idx_forum_reply_qa_sync` (`qa_sync_status`, `like_count`),
  KEY `idx_forum_reply_visibility_deleted` (`visibility`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='论坛回复表（双层嵌套）';

-- ============================================
-- 8. 知识库溯源表
-- 说明：
-- 1. 不存向量本体，向量交给 Qdrant
-- 2. 支持三类来源：
--    1论坛飞轮 2管理员手动添加 3种子数据导入
-- ============================================
CREATE TABLE `knowledge_qa_trace` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '雪花ID',
  `source_type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1论坛飞轮 2管理员手动添加 3种子数据导入',
  `source_post_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '来源主贴ID，手动导入时可为空',
  `source_reply_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '来源回复ID，手动导入时可为空',
  `contributor_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '知识贡献者用户ID',
  `created_by_admin_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '手动录入管理员ID',
  `source_post_title_snapshot` VARCHAR(120) DEFAULT NULL COMMENT '主贴标题快照',
  `source_post_content_snapshot` TEXT DEFAULT NULL COMMENT '主贴正文快照',
  `source_reply_content_snapshot` TEXT DEFAULT NULL COMMENT '回复正文快照',
  `source_file_name` VARCHAR(128) DEFAULT NULL COMMENT '若来自种子导入，记录文件名',
  `question_text` VARCHAR(1000) NOT NULL COMMENT '提纯后的Question',
  `answer_text` TEXT NOT NULL COMMENT '提纯后的Answer',
  `category` VARCHAR(64) DEFAULT NULL COMMENT '知识分类，如宿舍生活/报到迎新',
  `qa_json` JSON DEFAULT NULL COMMENT '严格QA JSON结构化结果',
  `context_text` TEXT NOT NULL COMMENT 'Q+A拼接文本，用于Embedding及检索上下文',
  `collection_name` VARCHAR(64) NOT NULL DEFAULT 'freshman_qa' COMMENT 'Qdrant集合名',
  `qdrant_point_id` VARCHAR(64) NOT NULL COMMENT 'Qdrant点ID',
  `vector_dimension` SMALLINT UNSIGNED NOT NULL DEFAULT 1536 COMMENT '向量维度',
  `embedding_model` VARCHAR(64) NOT NULL DEFAULT 'text-embedding-v2' COMMENT 'Embedding模型名',
  `llm_model` VARCHAR(64) NOT NULL DEFAULT 'qwen-plus' COMMENT '提纯模型名',
  `like_count_snapshot` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '触发入库时的点赞快照',
  `reward_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '给贡献者发放的积分',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0待提纯 1待向量化 2已入Qdrant 3失败 4已下线',
  `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
  `synced_at` DATETIME DEFAULT NULL COMMENT '成功写入Qdrant时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_knowledge_qa_trace_qdrant_point` (`qdrant_point_id`),
  UNIQUE KEY `uk_knowledge_qa_trace_source_reply` (`source_reply_id`),
  KEY `idx_knowledge_qa_trace_post` (`source_post_id`),
  KEY `idx_knowledge_qa_trace_user` (`contributor_user_id`),
  KEY `idx_knowledge_qa_trace_status_created` (`status`, `created_at`),
  KEY `idx_knowledge_qa_trace_source_type` (`source_type`, `status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库溯源表';

-- ============================================
-- 9. 管理员操作日志表
-- 超级管理员的删帖、封号、知识库手动录入等动作必须留痕
-- ============================================
CREATE TABLE `admin_operation_log` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '雪花ID',
  `admin_user_id` BIGINT UNSIGNED NOT NULL COMMENT '管理员ID',
  `target_type` TINYINT UNSIGNED NOT NULL COMMENT '1用户 2帖子 3回复 4知识库 6头衔',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `operation_type` VARCHAR(32) NOT NULL COMMENT 'BAN_USER/HIDE_POST/DELETE_REPLY/MANUAL_KB_ADD等',
  `before_data` JSON DEFAULT NULL COMMENT '操作前快照',
  `after_data` JSON DEFAULT NULL COMMENT '操作后快照',
  `reason` VARCHAR(255) DEFAULT NULL COMMENT '操作原因',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin_operation_log_admin_created` (`admin_user_id`, `created_at`),
  KEY `idx_admin_operation_log_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员操作日志表';

-- ============================================
-- 初始化数据
-- ============================================

-- 超级管理员
-- 注意：password_hash 这里是占位值，正式环境请替换成你自己生成的 BCrypt 哈希
INSERT INTO `sys_user` (
  `id`, `username`, `password_hash`, `nickname`, `gender`, `role`, `points`, `status`, `created_at`, `updated_at`, `deleted`
) VALUES
(
  1000000000000000001,
  'superadmin',
  'CHANGE_ME_BCRYPT_HASH',
  '超级管理员',
  0,
  9,
  0,
  1,
  NOW(),
  NOW(),
  0
);

INSERT INTO `user_stats` (
  `user_id`, `post_count`, `reply_count`, `reply_like_received_count`, `post_like_received_count`,
  `knowledge_contribution_count`, `featured_answer_count`,
  `last_calculated_at`, `created_at`, `updated_at`
) VALUES
(
  1000000000000000001, 0, 0, 0, 0, 0, 0, NOW(), NOW(), NOW()
);

-- 基础头衔
INSERT INTO `sys_title` (
  `id`, `title_name`, `title_code`, `description`, `grant_type`, `rule_type`, `rule_threshold`, `status`, `sort_no`, `created_at`, `updated_at`
) VALUES
(2000000000000000001, '热心答主', 'HELPFUL_ANSWERER', '累计回复数达到 20', 1, 1, 20, 1, 10, NOW(), NOW()),
(2000000000000000002, '高赞答主', 'LIKED_ANSWERER', '回复累计获赞数达到 100', 1, 2, 100, 1, 20, NOW(), NOW()),
(2000000000000000003, '知识共建者', 'KNOWLEDGE_CONTRIBUTOR', '知识库贡献数达到 5', 1, 3, 5, 1, 30, NOW(), NOW()),
(2000000000000000004, '校园百晓生', 'CAMPUS_EXPERT', '高质量回答数达到 10', 1, 4, 10, 1, 40, NOW(), NOW()),
(2000000000000000005, '官方认证学长', 'OFFICIAL_MENTOR', '由管理员手动授予的官方认证头衔', 2, NULL, NULL, 1, 50, NOW(), NOW());

-- ============================================
-- 可选说明
-- 1. 种子知识 data.jsonl 导入时：
--    source_type = 3
--    source_file_name = data.jsonl
--    contributor_user_id 可为空
--    created_by_admin_id 可填当前导入管理员
--
-- 2. 论坛高赞回复进入知识库时：
--    like_count == 10 触发 MQ
--    生成 QA 后写 knowledge_qa_trace
--    status 流转：0待提纯 -> 1待向量化 -> 2已入Qdrant
--
-- 3. 图文帖审核流程：
--    forum_post.status = 0
--    forum_post_image.status = 0
--    MQ 审核通过后统一切换为 1
-- ============================================
