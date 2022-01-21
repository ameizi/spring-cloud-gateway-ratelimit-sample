package com.anchnet.gateway.filter.ratelimit;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

/**
 * 重写RequestRateLimiterGatewayFilterFactory，自定义Json返回体
 */
@Slf4j
@Component
public class MyRequestRateLimiterGatewayFilterFactory extends RequestRateLimiterGatewayFilterFactory {

    private static final String EMPTY_KEY = "____EMPTY_KEY__";

    public MyRequestRateLimiterGatewayFilterFactory(RateLimiter defaultRateLimiter, KeyResolver defaultKeyResolver) {
        super(defaultRateLimiter, defaultKeyResolver);
    }

    @Override
    public GatewayFilter apply(Config config) {
        KeyResolver resolver = getOrDefault(config.getKeyResolver(), getDefaultKeyResolver());
        RateLimiter<Object> limiter = getOrDefault(config.getRateLimiter(), getDefaultRateLimiter());
        boolean denyEmpty = getOrDefault(config.getDenyEmptyKey(), this.isDenyEmptyKey());
        HttpStatusHolder emptyKeyStatus = HttpStatusHolder.parse(getOrDefault(config.getEmptyKeyStatus(), this.getEmptyKeyStatusCode()));
        return (exchange, chain) -> resolver.resolve(exchange).defaultIfEmpty(EMPTY_KEY).flatMap(key -> {
            if (EMPTY_KEY.equals(key)) {
                if (denyEmpty) {
                    setResponseStatus(exchange, emptyKeyStatus);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            }
            String routeId = config.getRouteId();
            if (routeId == null) {
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                routeId = route.getId();
            }
            return limiter.isAllowed(routeId, key).flatMap(response -> {

                for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                    exchange.getResponse().getHeaders().add(header.getKey(), header.getValue());
                }

                if (response.isAllowed()) {
                    return chain.filter(exchange);
                }

                // 限流处理,修改返回json
                // setResponseStatus(exchange, config.getStatusCode());
                // return exchange.getResponse().setComplete();
                return respJson(exchange);
            });
        });
    }

    private <T> T getOrDefault(T configValue, T defaultValue) {
        return (configValue != null) ? configValue : defaultValue;
    }

    private Mono<Void> respJson(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        JSONObject jsonObject = new JSONObject()
                .fluentPut("code", HttpStatus.TOO_MANY_REQUESTS.value())
                .fluentPut("msg", "请求太快了，你被限流了。。。")
                .fluentPut("ip", exchange.getRequest().getRemoteAddress().getAddress().getHostAddress())
                .fluentPut("data", null)
                .fluentPut("time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(LocalDateTime.now()));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer wrap = response.bufferFactory().wrap(jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
        log.info("请求太快了，被限流了。。。");
        return response.writeWith(Mono.just(wrap));
    }

}