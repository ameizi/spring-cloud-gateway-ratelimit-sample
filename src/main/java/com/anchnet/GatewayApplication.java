package com.anchnet;

import com.alibaba.cloud.sentinel.gateway.ConfigConstants;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        // 【重点】设置应用类型为 Spring Cloud Gateway
        System.setProperty(SentinelConfig.APP_TYPE, ConfigConstants.APP_TYPE_SCG_GATEWAY);
        SpringApplication.run(GatewayApplication.class, args);
    }

}
