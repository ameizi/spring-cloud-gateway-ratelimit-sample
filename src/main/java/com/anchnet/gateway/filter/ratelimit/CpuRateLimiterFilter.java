package com.anchnet.gateway.filter.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 基于CPU使用率的限流
 */
@Slf4j
@Component
public class CpuRateLimiterFilter implements GatewayFilter, Ordered {

    @Resource
    private MetricsEndpoint metricsEndpoint;
    private static final String METRIC_NAME = "system.cpu.usage";
    private static final double MAX_USAGE = 0.10D;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Double systemCpuUsage = metricsEndpoint.metric(METRIC_NAME, null)
                .getMeasurements()
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(MetricsEndpoint.Sample::getValue)
                .filter(Double::isFinite)
                .orElse(0.0D);
        boolean ok = systemCpuUsage < MAX_USAGE;
        log.debug("system.cpu.usage: " + systemCpuUsage + " ok: " + ok);
        if (!ok) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        } else {
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

}