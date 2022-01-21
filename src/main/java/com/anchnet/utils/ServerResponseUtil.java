package com.anchnet.utils;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ServerResponseUtil {

    public static Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
        JSONObject jsonObject = new JSONObject()
                .fluentPut("code", HttpStatus.TOO_MANY_REQUESTS.value())
                .fluentPut("msg", "请求被拦截，拦截类型为 " + t.getClass().getSimpleName())
                .fluentPut("ip", exchange.getRequest().getRemoteAddress().getAddress().getHostAddress())
                .fluentPut("data", null)
                .fluentPut("time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(LocalDateTime.now()));
        return ServerResponse.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonObject);
    }

}
