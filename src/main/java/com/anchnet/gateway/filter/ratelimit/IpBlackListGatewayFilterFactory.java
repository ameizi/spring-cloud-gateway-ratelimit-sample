package com.anchnet.gateway.filter.ratelimit;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * IP黑名单过滤器
 */
@Slf4j
@Component
public class IpBlackListGatewayFilterFactory extends AbstractGatewayFilterFactory<IpBlackListGatewayFilterFactory.Config> {

    public IpBlackListGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String ipAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            if (config.blacklist.contains(ipAddress)) {
                return respJson(exchange);
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {

        private Set<String> blacklist = new HashSet<>();

        public Set<String> getBlacklist() {
            return blacklist;
        }

        public void setBlacklist(Set<String> blacklist) {
            this.blacklist = blacklist;
        }
    }

    private Mono<Void> respJson(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        JSONObject jsonObject = new JSONObject()
                .fluentPut("code", HttpStatus.FORBIDDEN.value())
                .fluentPut("msg", "IP黑名单，拒绝访问。。。")
                .fluentPut("ip", exchange.getRequest().getRemoteAddress().getAddress().getHostAddress())
                .fluentPut("data", null)
                .fluentPut("time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(LocalDateTime.now()));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer wrap = response.bufferFactory().wrap(jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
        log.info("IP黑名单，拒绝访问。。。");
        return response.writeWith(Mono.just(wrap));
    }
}
