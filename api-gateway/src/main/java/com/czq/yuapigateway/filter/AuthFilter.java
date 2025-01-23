package com.czq.yuapigateway.filter;

import com.czq.apiclientsdk.utils.SignUtils;
import com.czq.apicommon.entity.User;
import com.czq.apicommon.service.ApiBackendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class AuthFilter implements GatewayFilter, Ordered {

    @DubboReference
    private ApiBackendService apiBackendService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        String accessKey = headers.getFirst("accessKey");
        String body = headers.getFirst("body");
        String sign = headers.getFirst("sign");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");

        // 1. 验证 accessKey
        User invokeUser;
        try {
            invokeUser = apiBackendService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("远程调用获取用户信息失败", e);
            return FilterUtils.handleNoAuth(exchange.getResponse());
        }

        if (invokeUser == null) {
            log.error("accessKey 无效");
            return FilterUtils.handleNoAuth(exchange.getResponse());
        }

        // 2. 验证签名
        String secretKey = invokeUser.getSecretKey();
        String serverSign = SignUtils.generateSign(body, secretKey);
        if (!serverSign.equals(sign)) {
            log.error("签名校验失败");
            return FilterUtils.handleNoAuth(exchange.getResponse());
        }

        // 3. 防重放攻击
        Boolean isNonceValid = stringRedisTemplate.opsForValue()
                .setIfAbsent(nonce, "1", 5, TimeUnit.MINUTES);
        if (isNonceValid == null || !isNonceValid) {
            log.error("重复请求或随机数无效");
            return FilterUtils.handleNoAuth(exchange.getResponse());
        }

        // 存储用户信息到请求上下文
        exchange.getAttributes().put("invokeUser", invokeUser);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10; // 第二个执行
    }
}
