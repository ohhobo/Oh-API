package com.yupi.springbootinit.utils;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.czq.apicommon.entity.InterfaceInfo;
import com.yupi.springbootinit.mapper.InterfaceInfoMapper;
import com.yupi.springbootinit.service.InterfaceInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sun.text.SupplementaryCharacterData;

import java.util.Set;

import static com.czq.apicommon.constant.RedisConstant.RANKING_KEY;


/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/01/29/15:32
 * @Description:
 */
@Component
@RequiredArgsConstructor
public class RankingSyncTask {
    private final StringRedisTemplate stringRedisTemplate;
    private final InterfaceInfoService interfaceInfoService;
    // 每天凌晨3点同步数据
    @Scheduled(cron = "0 0 3 * * ?")
    public void syncRankingToDB() {
        Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                .rangeWithScores(RANKING_KEY, 0, -1);

        tuples.forEach(tuple -> {
            Long interfaceId = Long.parseLong(tuple.getValue());
            int score = tuple.getScore().intValue();

            UpdateWrapper<InterfaceInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", interfaceId);
            updateWrapper.set("likeCount",score);
            interfaceInfoService.update(updateWrapper);
        });
    }
}
