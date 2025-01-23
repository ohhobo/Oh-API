package com.czq.yuapigateway.config;

import com.czq.yuapigateway.filter.AuthFilter;
import com.czq.yuapigateway.filter.InterfaceCheckFilter;

import com.czq.yuapigateway.filter.InvokeCountFilter;
import com.czq.yuapigateway.filter.LoggingFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GatewayConfig {


    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, LoggingFilter loggingFilter,
                                           AuthFilter authFilter,
                                           InterfaceCheckFilter interfaceCheckFilter,
                                           InvokeCountFilter invokeCountFilter) {
        //用路由前缀区分路由来源是前端还是接口管理平台
        return builder.routes()
                .route(r ->
                        r.path("/api/interface/**")
                                .filters(f -> f.filters(
                                        loggingFilter,
                                        authFilter,
                                        interfaceCheckFilter,
                                        invokeCountFilter
                                ))
                                .uri("lb://api-interface")
                )
                .build();
    }



}
