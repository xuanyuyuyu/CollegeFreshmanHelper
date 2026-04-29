DELETE t1
FROM sys_user_title t1
JOIN sys_user_title t2
  ON t1.user_id = t2.user_id
 AND (
    IFNULL(t1.grant_source, 1) < IFNULL(t2.grant_source, 1)
    OR (IFNULL(t1.grant_source, 1) = IFNULL(t2.grant_source, 1) AND IFNULL(t1.is_wearing, 0) < IFNULL(t2.is_wearing, 0))
    OR (IFNULL(t1.grant_source, 1) = IFNULL(t2.grant_source, 1) AND IFNULL(t1.is_wearing, 0) = IFNULL(t2.is_wearing, 0) AND IFNULL(t1.granted_at, '1970-01-01 00:00:00') < IFNULL(t2.granted_at, '1970-01-01 00:00:00'))
    OR (IFNULL(t1.grant_source, 1) = IFNULL(t2.grant_source, 1) AND IFNULL(t1.is_wearing, 0) = IFNULL(t2.is_wearing, 0) AND IFNULL(t1.granted_at, '1970-01-01 00:00:00') = IFNULL(t2.granted_at, '1970-01-01 00:00:00') AND t1.id < t2.id)
 );

ALTER TABLE `sys_user_title`
  DROP INDEX `uk_sys_user_title_user_title`,
  ADD UNIQUE KEY `uk_sys_user_title_user` (`user_id`);
