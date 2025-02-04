package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.czq.apicommon.entity.SeckillActivity;
import com.czq.apicommon.entity.SeckillMessage;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.mapper.SeckillActivityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static com.czq.apicommon.constant.RedisConstant.SECKILL_STOCK;
import static com.czq.apicommon.constant.RedisConstant.SECKILL_USER;
import static com.czq.apicommon.constant.RabbitmqConstant.SECKILL_EXCHANGE;
import static com.czq.apicommon.constant.RabbitmqConstant.SECKILL_ROUTING_KEY;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: cyr
 * @Date: 2025/02/04/16:28
 * @Description:
 */
@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final SeckillActivityMapper seckillActivityMapper;

    // 发布秒杀活动（并触发预热）
    @PostMapping("/publish")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<String> publishActivity(@RequestBody SeckillActivity activity) {
        // 1. 保存活动到数据库
        activity.setStatus(1); // 状态设置为进行中
        seckillActivityMapper.insert(activity);

        // 2. 预热库存到Redis
        warmUpStock(activity);

        return ResultUtils.success("活动发布成功");
    }

    // 修改活动状态
    @PostMapping("/status")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<String> updateActivityStatus(
            @RequestParam Long activityId,
            @RequestParam Integer status
    ) {
        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        activity.setStatus(status);
        seckillActivityMapper.updateById(activity);

        // 触发库存预热或清理
        if (status == 1) {
            warmUpStock(activity);
        } else if (status == 2) {
            clearRedisData(activityId);
        }

        return ResultUtils.success("状态更新成功");
    }

    private void warmUpStock(SeckillActivity activity) {
        String stockKey = SECKILL_STOCK + activity.getId();
        String usersKey = SECKILL_USER + activity.getId();
        stringRedisTemplate.opsForValue().set(stockKey, activity.getTotalStock().toString());
        stringRedisTemplate.delete(usersKey);
    }

    private void clearRedisData(Long activityId) {
        stringRedisTemplate.delete(SECKILL_STOCK + activityId);
        stringRedisTemplate.delete(SECKILL_USER + activityId);
    }
    @PostMapping("/{activityId}")
    public BaseResponse<String> seckill(@PathVariable Long activityId,
                                @RequestHeader Long userId) {
        SeckillActivity seckillActivity = seckillActivityMapper.selectById(activityId);
        Integer stockPerUser = seckillActivity.getStockPerUser();
        // 构造Redis Key
        String stockKey = SECKILL_STOCK + activityId;
        String usersKey = SECKILL_USER + activityId;

        // 执行Lua脚本
        DefaultRedisScript<List> script = new DefaultRedisScript<>(
                loadScript("seckill.lua"),
                List.class
        );
        List<Long> result = stringRedisTemplate.execute(
                script,
                Arrays.asList(stockKey, usersKey),
                userId.toString(),
                stockPerUser // 从数据库读取实际值
        );

        // 处理结果
        if (result.get(0) == 3) {
            rabbitTemplate.convertAndSend(
                    SECKILL_EXCHANGE,
                    SECKILL_ROUTING_KEY,
                    new SeckillMessage(activityId, userId, result.get(1))
            );
            return ResultUtils.success("抢购成功");
        } else {
            String msg = switch (result.get(0).intValue()) {
                case 0 -> "活动不存在";
                case 1 -> "库存不足";
                case 2 -> "请勿重复参与";
                default -> "系统繁忙";
            };
            return ResultUtils.error(500,msg);
        }
    }

    private String loadScript(String path) {
        StringBuilder script = new StringBuilder();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: " + path);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 根据需要处理异常，例如可以抛出自定义异常或封装此异常
        }
        return script.toString();
    }
}
