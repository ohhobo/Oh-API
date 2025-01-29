package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.vo.InterfaceRankVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/01/29/15:08
 * @Description:
 */
public interface RankingService {
    List<InterfaceRankVO> getTopNApis(int topN);
    void updateRanking(Long interfaceId, int delta);
}
