CREATE TABLE `forum_like_record` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '雪花ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID',
  `target_type` TINYINT UNSIGNED NOT NULL COMMENT '1帖子 2回复',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `like_status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '0已取消 1已点赞',
  `liked_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次点赞时间',
  `unliked_at` DATETIME DEFAULT NULL COMMENT '最近一次取消点赞时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_forum_like_record_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_forum_like_record_user_status_liked_at` (`user_id`, `like_status`, `liked_at`),
  KEY `idx_forum_like_record_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='论坛点赞明细表';
