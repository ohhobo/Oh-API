package com.czq.apicommon.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

// 点赞记录表实体
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("interface_like")
public class InterfaceLike {
    @TableId(type = IdType.AUTO)
    private Long id;

    /*
     * 用户id
     */
    private Long userId;

    /*
     * 接口id
     */
    private Long interfaceId;

    /*
     * 状态 0-未点赞 1-已点赞
     */
    private Integer status;

    /*
     * 创建时间
     */
    private Date createTime;

    /*
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
