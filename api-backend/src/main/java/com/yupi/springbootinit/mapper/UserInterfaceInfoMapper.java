package com.yupi.springbootinit.mapper;

import com.czq.apicommon.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.vo.UserInterfaceInfoAnalysisVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Entity com.yupi.springbootinit.model.entity.UserInterfaceInfo
 */
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {


    List<UserInterfaceInfoAnalysisVo> listTopInterfaceInfo(@Param("size") int size);

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);

    @Update("UPDATE user_interface SET left_num = left_num + #{num} " +
            "WHERE user_id = #{userId} AND interface_id = #{interfaceId}")
    int incrementSeckillNum(@Param("userId") Long userId,
                     @Param("interfaceId") Long interfaceId,
                     @Param("num") Integer num);

    @Insert("INSERT INTO user_interface (user_id, interface_id, left_num) " +
            "VALUES (#{userId}, #{interfaceId}, #{num}) " +
            "ON DUPLICATE KEY UPDATE left_num = left_num + #{num}")
    int insertOrUpdate(@Param("userId") Long userId,
                       @Param("interfaceId") Long interfaceId,
                       @Param("num") Long num);
}




