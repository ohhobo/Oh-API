package com.czq.yuapigateway.filter;

import com.czq.apicommon.entity.InterfaceInfo;
import com.czq.apicommon.entity.User;
import com.czq.apicommon.service.ApiBackendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

@Component
@Slf4j
public class InvokeCountFilter implements GatewayFilter, Ordered {

    @DubboReference
    private ApiBackendService apiBackendService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 从上下文获取用户和接口信息
        User invokeUser = exchange.getAttribute("invokeUser");
        InterfaceInfo interfaceInfo = exchange.getAttribute("interfaceInfo");

        if (invokeUser == null || interfaceInfo == null) {
            return FilterUtils.handleNoAuth(exchange.getResponse());
        }

        // 执行调用统计
        boolean isSuccess;
        try {
            isSuccess = apiBackendService.invokeCount(
                    invokeUser.getId(),
                    interfaceInfo.getId()
            );
        } catch (Exception e) {
            log.error("调用统计失败", e);
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }

        if (!isSuccess) {
            log.error("接口调用次数不足");
            return FilterUtils.handleNoAuth(exchange.getResponse());
        }

        // 装饰响应对象
        return FilterUtils.handleResponse(
                exchange,
                chain,
                interfaceInfo.getId(),
                invokeUser.getId(),
                rabbitTemplate
        );
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 30; // 最后执行
    }
}