package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czq.apicommon.entity.InterfaceLike;
import com.yupi.springbootinit.model.vo.InterfaceRankVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/01/29/14:36
 * @Description:
 */
public interface InterfaceLikeService extends IService<InterfaceLike> {
    void toggleLike(Long userId, Long interfaceId);

    Integer getLikeCount(Long interfaceId);

}
