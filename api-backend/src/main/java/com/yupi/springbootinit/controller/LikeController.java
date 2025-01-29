package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.vo.InterfaceRankVO;
import com.yupi.springbootinit.service.InterfaceLikeService;
import com.yupi.springbootinit.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
