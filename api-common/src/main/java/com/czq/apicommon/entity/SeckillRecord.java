package com.czq.apicommon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/02/04/15:53
 * @Description:
 */
// 秒杀记录实体
@Data
@TableName("seckill_record")
@Builder
public class SeckillRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long activityId;
    private Long obtainedNum;
    private LocalDateTime createdTime;
}
