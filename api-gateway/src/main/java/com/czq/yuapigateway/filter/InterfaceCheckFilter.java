package com.czq.yuapigateway.filter;

import com.czq.apicommon.entity.InterfaceInfo;
import com.czq.apicommon.service.ApiBackendService;
import com.czq.yuapigateway.utils.FilterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class InterfaceCheckFilter implements GatewayFilter, Ordered {

    @DubboReference
    private ApiBackendService apiBackendService;

    private static final String INTERFACE_HOST = "http://localhost:8123";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = INTERFACE_HOST + request.getPath().value();
        String method = request.getMethod().toString();

        // 查询接口信息
        InterfaceInfo interfaceInfo;
        try {
            interfaceInfo = apiBackendService.getInterFaceInfo(path, method);
        } catch (Exception e) {
            log.error("远程调用获取接口信息失败", e);
            return FilterUtils.handleNoAuth(exchange.getResponse());
        }

        if (interfaceInfo == null) {
            log.error("接口不存在: {} {}", method, path);
            return FilterUtils.handleNoAuth(exchange.getResponse());
        }

        // 存储接口信息到请求上下文
        exchange.getAttributes().put("interfaceInfo", interfaceInfo);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20; // 第三个执行
    }
}