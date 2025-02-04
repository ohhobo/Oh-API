package com.yupi.springbootinit.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.czq.apicommon.entity.SeckillActivity;
import com.czq.apicommon.entity.SeckillMessage;
import com.czq.apicommon.entity.SeckillRecord;
import com.yupi.springbootinit.mapper.SeckillActivityMapper;
import com.yupi.springbootinit.mapper.SeckillRecordMapper;
import com.yupi.springbootinit.mapper.UserInterfaceInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import static com.czq.apicommon.constant.RabbitmqConstant.SECKILL_QUEUE;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/02/04/17:06
 * @Description:
 */
@Component
@RequiredArgsConstructor
public class SeckillListener {

    private final SeckillRecordMapper seckillRecordMapper;
    private final SeckillActivityMapper seckillActivityMapper;
    private final UserInterfaceInfoMapper userInterfaceInfoMapper;

    @RabbitListener(queues = SECKILL_QUEUE)
    public void processMessage(SeckillMessage message) {
        // 1. 写入秒杀记录（防重）
        if (seckillRecordMapper.exists(
                new LambdaQueryWrapper<SeckillRecord>()
                        .eq(SeckillRecord::getUserId, message.getUserId())
                        .eq(SeckillRecord::getActivityId, message.getActivityId())
        )) return;

        SeckillRecord record = SeckillRecord.builder()
                .userId(message.getUserId())
                .activityId(message.getActivityId())
                .obtainedNum(message.getNum())
                .build();

        seckillRecordMapper.insert(record);

        // 2. 更新用户接口次数
        userInterfaceInfoMapper.insertOrUpdate(
                message.getUserId(),
                getInterfaceIdByActivity(message.getActivityId()), // 查询活动关联接口
                message.getNum()
        );
    }

    private Long getInterfaceIdByActivity(Long activityId) {
        // 从数据库或缓存查询接口ID
        SeckillActivity seckillActivity = seckillActivityMapper.selectById(activityId);
        Long interfaceId = seckillActivity.getInterfaceId();
        return interfaceId;
    }
}
