-- 为开发联调补齐可登录的超级管理员账号
-- username: superadmin
-- password: Admin@123456
-- 上线前请务必修改默认密码

INSERT INTO `sys_user` (
  `id`,
  `username`,
  `password_hash`,
  `nickname`,
  `gender`,
  `role`,
  `points`,
  `status`,
  `created_at`,
  `updated_at`,
  `deleted`
) VALUES (
  1000000000000000001,
  'superadmin',
  '$2y$10$uIVuRbpCL82VxYPtSkDhoOeTb59UadsDMI6boVBwHl.XXX3WT7Hdm',
  '超级管理员',
  0,
  9,
  0,
  1,
  NOW(),
  NOW(),
  0
)
ON DUPLICATE KEY UPDATE
  `password_hash` = IF(`password_hash` = 'CHANGE_ME_BCRYPT_HASH', VALUES(`password_hash`), `password_hash`),
  `nickname` = IF(`nickname` IS NULL OR `nickname` = '', VALUES(`nickname`), `nickname`),
  `role` = IF(`role` < 9, 9, `role`),
  `status` = 1,
  `deleted` = 0,
  `updated_at` = NOW();

INSERT INTO `user_stats` (
  `user_id`,
  `post_count`,
  `reply_count`,
  `reply_like_received_count`,
  `post_like_received_count`,
  `knowledge_contribution_count`,
  `featured_answer_count`,
  `reward_applied_count`,
  `reward_passed_count`,
  `last_calculated_at`,
  `created_at`,
  `updated_at`
) VALUES (
  1000000000000000001,
  0,
  0,
  0,
  0,
  0,
  0,
  0,
  0,
  NOW(),
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE
  `updated_at` = NOW();
