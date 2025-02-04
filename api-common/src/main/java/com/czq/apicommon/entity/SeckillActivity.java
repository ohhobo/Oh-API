package com.czq.apicommon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/02/04/15:48
 * @Description:
 */
// 秒杀活动实体
@Data
@TableName("seckill_activity")
public class SeckillActivity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long interfaceId;

    private Integer totalStock;

    private Integer stockPerUser;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 当前活动状态0未开始/1进行中/2已结束
     */
    private Integer status;

    private LocalDateTime createdTime;
}
