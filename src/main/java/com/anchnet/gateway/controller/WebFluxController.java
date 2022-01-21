package com.anchnet.gateway.controller;

import com.anchnet.gateway.utils.Bucket4jUtil;
import io.github.bucket4j.Bucket;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Objects;

@RestController
public class WebFluxController {

    private static Bucket bucket = Bucket4jUtil.getBucket();

    @GetMapping("/orders/list")
    public Mono<String> orders() {
        return Mono.just("orders list");
    }

    @GetMapping("/products/list")
    public Mono<String> products() {
        return Mono.just("products list");
    }

    @GetMapping("/users/list")
    public Mono<String> users() {
        return Mono.just("users list");
    }

    @GetMapping("/accounts/list")
    public Mono<String> accounts() {
        return Mono.just("accounts list");
    }

    @GetMapping("/try_bucket4j")
    public Mono<String> get(ServerWebExchange exchange) {
        if (bucket.tryConsume(1)) {
            return Mono.just("Hello,Bucket4j");
        } else {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return Mono.just(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
        }
    }

    @Resource
    private MetricsEndpoint metricsEndpoint;

    @GetMapping("/metrics/cpu-usage")
    public Mono<Double> cpu_usage() {
        Double systemCpuUsage = metricsEndpoint.metric("system.cpu.usage", null)
                .getMeasurements()
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(MetricsEndpoint.Sample::getValue)
                .filter(Double::isFinite)
                .orElse(0.0D);
        return Mono.just(systemCpuUsage);
    }

}
