package com.yupi.springbootinit.service.impl;

import com.czq.apicommon.entity.InterfaceLike;
import com.yupi.springbootinit.model.vo.InterfaceRankVO;
import com.yupi.springbootinit.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.czq.apicommon.constant.RedisConstant.RANKING_KEY;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/01/29/15:09
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<InterfaceRankVO> getTopNApis(int topN) {
        return stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, 0, topN - 1)
                .stream()
                .map(tuple -> new InterfaceRankVO(
                        Long.parseLong(tuple.getValue()),
                        tuple.getScore().intValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void updateRanking(Long apiId, int delta) {
        stringRedisTemplate.opsForZSet().incrementScore(
                RANKING_KEY,
                apiId.toString(),
                delta
        );
    }
}