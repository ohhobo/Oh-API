-- 1. 点赞记录表（新增）
CREATE TABLE `interface_like` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `userId` BIGINT NOT NULL COMMENT '用户ID',
  `interfaceId` BIGINT NOT NULL COMMENT '接口ID',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1-点赞 0-取消',
  `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_api` (`userId`,`interfaceId`) -- 唯一约束防止重复点赞
);