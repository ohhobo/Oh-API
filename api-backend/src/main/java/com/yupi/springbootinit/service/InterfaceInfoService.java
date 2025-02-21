package com.yupi.springbootinit.service;

import com.czq.apicommon.entity.InterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void incrementLikeCount(Long interfaceId, int delta);

    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
