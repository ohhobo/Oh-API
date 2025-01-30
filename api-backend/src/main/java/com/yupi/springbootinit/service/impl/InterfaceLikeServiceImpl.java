package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czq.apicommon.entity.InterfaceLike;
import com.yupi.springbootinit.mapper.InterfaceLikeMapper;
import com.yupi.springbootinit.service.InterfaceInfoService;
import com.yupi.springbootinit.service.InterfaceLikeService;
import com.yupi.springbootinit.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.Rank;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.czq.apicommon.constant.RedisConstant.RANKING_PUSH_KEY;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/01/29/14:50
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class InterfaceLikeServiceImpl extends ServiceImpl<InterfaceLikeMapper, InterfaceLike> implements InterfaceLikeService {
    private final InterfaceInfoService interfaceInfoService;
    private final RankingService rankingService;
    private final StringRedisTemplate stringRedisTemplate;
    @Override
    @Transactional
    public void toggleLike(Long userId, Long interfaceId) {
        // 使用 MyBatis-Plus 提供的 LambdaQueryWrapper
        LambdaQueryWrapper<InterfaceLike> queryWrapper = new LambdaQueryWrapper<InterfaceLike>()
                .eq(InterfaceLike::getUserId, userId)
                .eq(InterfaceLike::getInterfaceId, interfaceId);

        InterfaceLike existLike = this.getOne(queryWrapper);
        int delta = 1;

        if (existLike != null) {
            delta = existLike.getStatus() == 1 ? -1 : 1;
            existLike.setStatus(1 - existLike.getStatus());
            this.updateById(existLike); // 使用父类方法
        } else {
            InterfaceLike newLike = InterfaceLike
                    .builder()
                    .userId(userId)
                    .interfaceId(interfaceId)
                    .status(1)
                    .build();
            this.save(newLike); // 使用父类方法
        }

        // 调用其他 Service
        interfaceInfoService.incrementLikeCount(interfaceId, delta);
        rankingService.updateRanking(interfaceId, delta);
        stringRedisTemplate.convertAndSend(RANKING_PUSH_KEY, interfaceId.toString());
    }

    @Override
    public Integer getLikeCount(Long apiId) {
        return interfaceInfoService.getById(apiId).getLikeCount();
    }

}

