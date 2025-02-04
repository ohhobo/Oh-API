-- 秒杀活动表
CREATE TABLE `seckill_activity` (
                                    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
                                    `interface_id` BIGINT NOT NULL COMMENT '接口ID',
                                    `total_stock` INT NOT NULL COMMENT '总库存（份数）',
                                    `stock_per_user` INT NOT NULL COMMENT '每份包含的调用次数',
                                    `start_time` DATETIME NOT NULL COMMENT '开始时间',
                                    `end_time` DATETIME NOT NULL COMMENT '结束时间',
                                    `status` TINYINT(1) DEFAULT 0 COMMENT '0-未开始 1-进行中 2-已结束',
                                    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 秒杀记录表（防重表）
CREATE TABLE `seckill_record` (
                                  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  `user_id` BIGINT NOT NULL,
                                  `activity_id` BIGINT NOT NULL,
                                  `obtained_num` INT NOT NULL COMMENT '获得的调用次数',
                                  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  UNIQUE KEY `uk_user_activity` (`user_id`,`activity_id`)
);