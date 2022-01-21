package com.anchnet.gateway.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class WebFluxController {

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

    /**
     * 流量控制
     *
     * @return
     */
    @GetMapping("/qps")
    public Mono<String> flow() {
        return Mono.just("qps流量控制");
    }

    /**
     * 服务降级
     *
     * @param name
     * @return
     */
    @GetMapping(value = "/degrade")
    public Mono<String> degrade(@RequestParam(required = false) String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name不能为空");
        }
        return Mono.just("degrade:" + name);
    }

    /**
     * 热点参数限流
     * <p>
     * 注意:
     * 热点参数限流必须加@SentinelResource注解，其资源值和@SentinelResource注解中value属性的值保持一致;
     * 热点参数限流方法声明必须抛出BlockException异常，否则全局异常捕获不到
     *
     * @param productId
     * @return
     */
    @GetMapping(value = "/hot")
    @SentinelResource(value = "hot")
    public Mono<String> hot(@RequestParam("productId") String productId) throws BlockException {
        return Mono.just("productId:" + productId);
    }

    /**
     * 授权规则
     * <p>
     * SentinelGatewayConfig中的setRequestOriginParser会从请求头或请求参数中拦截origin的值，根据该值进行白名单或黑名单处理。 若获取不到origin参数的值默认为远程IP。具体参考@com.anchnet.gateway.config.SentinelGatewayConfig
     *
     * @param origin
     * @return
     */
    @GetMapping(value = "/auth")
    public Mono<String> auth(@RequestParam("origin") String origin) {
        return Mono.just("origin:" + origin);
    }

}
