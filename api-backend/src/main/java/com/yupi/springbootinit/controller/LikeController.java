package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.vo.InterfaceRankVO;
import com.yupi.springbootinit.service.InterfaceLikeService;
import com.yupi.springbootinit.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import static com.czq.apicommon.constant.RedisConstant.RANKING_PUSH_KEY;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/01/29/15:22
 * @Description:
 */
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {
    private final InterfaceLikeService interfaceLikeService;
    private final RankingService rankingService;
    // SSE实时推送
    private final Set<SseEmitter> emitters = new CopyOnWriteArraySet<>();
    private final StringRedisTemplate stringRedisTemplate;

    @PostMapping("/toggle")
    public void toggleLike(@RequestParam Long apiId,
                                         @RequestParam Long userId) {
        interfaceLikeService.toggleLike(userId, apiId);
    }

    @GetMapping("/count")
    public BaseResponse<Integer> getLikeCount(@RequestParam Long apiId) {
        return ResultUtils.success(interfaceLikeService.getLikeCount(apiId));
    }

    @GetMapping("/ranking")
    public BaseResponse<List<InterfaceRankVO>> getRanking(@RequestParam(defaultValue = "5") int topN) {
        return ResultUtils.success(rankingService.getTopNApis(topN));
    }

    @GetMapping("/stream")
    public SseEmitter streamRanking() {
        SseEmitter emitter = new SseEmitter(180_000L); // 3分钟超时
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    @PostConstruct
    public void initRedisListener() {
        stringRedisTemplate.getConnectionFactory().getConnection().subscribe(
                (message, pattern) -> {
                    String interfaceId = new String(message.getBody());
                    emitters.forEach(emitter -> {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("update")
                                    .data(interfaceId));
                        } catch (IOException e) {
                            emitter.complete();
                            emitters.remove(emitter);
                        }
                    });
                },
                RANKING_PUSH_KEY.getBytes()
        );
    }
}
