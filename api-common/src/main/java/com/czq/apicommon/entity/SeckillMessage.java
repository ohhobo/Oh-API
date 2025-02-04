package com.czq.apicommon.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/02/04/16:37
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessage {
    private Long activityId;
    private Long userId;
    private Long num;
}
