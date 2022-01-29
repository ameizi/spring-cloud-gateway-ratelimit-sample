package com.anchnet.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.WebFluxCallbackManager;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.anchnet.utils.ServerResponseUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Objects;

@Configuration
public class GatewaySentinelConfig {

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    public GatewaySentinelConfig() {
        // 注入RequestOriginParser
        WebFluxCallbackManager.setRequestOriginParser(GatewaySentinelConfig::parseRequestOriginParser);
        // 注入BlockHandler
        WebFluxCallbackManager.setBlockHandler(ServerResponseUtil::handleRequest);
        // 注入RequestOriginParser
        GatewayCallbackManager.setRequestOriginParser(GatewaySentinelConfig::parseRequestOriginParser);
        // 注入BlockHandler
        GatewayCallbackManager.setBlockHandler(ServerResponseUtil::handleRequest);
    }

    public static String parseRequestOriginParser(ServerWebExchange exchange) {
        String origin;
        ServerHttpRequest request = exchange.getRequest();
        // 请求头
        List<String> origins = request.getHeaders().get("origin");
        if (CollectionUtils.isEmpty(origins)) {
            // 请求参数
            origin = request.getQueryParams().getFirst("origin");
        } else {
            origin = origins.get(0);
        }
        return !StringUtils.isEmpty(origin) ? origin : Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
    }

}
