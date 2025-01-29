package com.yupi.springbootinit.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/01/29/14:38
 * @Description:
 */
@Data
@AllArgsConstructor
public class InterfaceRankVO {
    private Long interfaceId;

    private Integer likeCount;
}
