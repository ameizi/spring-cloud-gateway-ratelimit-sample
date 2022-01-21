package com.anchnet.gateway.config;

import com.anchnet.gateway.filter.ratelimit.Bucket4jRateLimiterFilter;
import com.anchnet.gateway.filter.ratelimit.CpuRateLimiterFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

@Component
public class GatewayConfig {

    /**
     * 基于CPU使用率的动态限流
     */
    @Resource
    private CpuRateLimiterFilter cpuRateLimiterFilter;

    /**
     * 使用bucket4j基于请求参数限流
     */
    @Resource
    private Bucket4jRateLimiterFilter bucket4jRateLimiterFilter;

    /**
     * 根据URI限流
     *
     * @return
     */
    @Bean(name = "uriKeyResolver")
    @Primary
    public KeyResolver uriKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getURI().getPath());
    }

    /**
     * 根据用户限流
     */
    @Bean(name = "userKeyResolver")
    KeyResolver userKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            String userIdentity = request.getQueryParams().getFirst("user");
            userIdentity = StringUtils.isEmpty(userIdentity) ? request.getHeaders().getFirst("token") : userIdentity;
            userIdentity = StringUtils.isEmpty(userIdentity) ? request.getRemoteAddress().getAddress().getHostAddress() : userIdentity;
            return Mono.just(userIdentity);
        };
    }

    /**
     * 根据请求者IP进行限流
     */
    @Bean(name = "ipAddressKeyResolver")
    public KeyResolver ipAddressKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }


    /**
     * 动态路由配置
     *
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator customerRouteLocator(RouteLocatorBuilder builder) {
        // @formatter:off
        return builder.routes()
                .route(r -> r.path("/bucket")
                        // 自定义filter使用bucket4j进行限流
                        .filters(f -> f.filter(bucket4jRateLimiterFilter).setPath("/get"))
                        .uri("http://httpbin.org")
                        .order(0)
                        .id("orders")
                )
                .route(r -> r.path("/cpu")
                        // 自定义filter根据cpu使用率进行限流
                        .filters(f -> f.filter(cpuRateLimiterFilter).setPath("/metrics/cpu-usage"))
                        .uri("http://localhost:9000")
                        .order(0)
                        .id("cpu")
                )
                .build();
        // @formatter:on
    }
}
