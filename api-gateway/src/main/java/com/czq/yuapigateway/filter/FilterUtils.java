package com.czq.yuapigateway.filter;

import com.czq.apiclientsdk.utils.SignUtils;
import com.czq.apicommon.entity.InterfaceInfo;
import com.czq.apicommon.entity.User;
import com.czq.apicommon.service.ApiBackendService;
import com.czq.apicommon.vo.UserInterfaceInfoMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.czq.apicommon.constant.RabbitmqConstant.EXCHANGE_INTERFACE_CONSISTENT;
import static com.czq.apicommon.constant.RabbitmqConstant.ROUTING_KEY_INTERFACE_CONSISTENT;

@Slf4j
public class FilterUtils {

    public static Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public static Mono<Void> handleResponse(ServerWebExchange exchange,
                                            GatewayFilterChain chain,
                                            long interfaceInfoId,
                                            long userId,
                                            RabbitTemplate rabbitTemplate) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        if (originalResponse.getStatusCode() == HttpStatus.OK) {
            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                        return super.writeWith(fluxBody.map(dataBuffer -> {
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            DataBufferUtils.release(dataBuffer);

                            // 记录响应日志
                            String responseBody = new String(content, StandardCharsets.UTF_8);
                            log.info("响应状态码: {}", getStatusCode());
                            log.info("响应内容: {}", responseBody);

                            // 接口调用失败回滚统计
                            if (getStatusCode() != HttpStatus.OK) {
                                UserInterfaceInfoMessage message =
                                        new UserInterfaceInfoMessage(userId, interfaceInfoId);
                                rabbitTemplate.convertAndSend(
                                        EXCHANGE_INTERFACE_CONSISTENT,
                                        ROUTING_KEY_INTERFACE_CONSISTENT,
                                        message
                                );
                            }
                            return bufferFactory.wrap(content);
                        }));
                    }
                    return super.writeWith(body);
                }
            };
            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }
        return chain.filter(exchange);
    }
}
