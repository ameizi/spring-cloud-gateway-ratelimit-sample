package com.anchnet.gateway.controller;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@RestController
public class RulesWebFluxController {

    @GetMapping("/api_rules")
    public Mono<Set<ApiDefinition>> apiRules() {
        return Mono.just(GatewayApiDefinitionManager.getApiDefinitions());
    }

    @GetMapping("/gateway_rules")
    public Mono<Set<GatewayFlowRule>> gatewayRules() {
        return Mono.just(GatewayRuleManager.getRules());
    }

    @GetMapping("/flow_rules")
    public Mono<List<FlowRule>> flowRules() {
        return Mono.just(FlowRuleManager.getRules());
    }

    @GetMapping("/param_rules")
    public Mono<List<ParamFlowRule>> paramRules() {
        return Mono.just(ParamFlowRuleManager.getRules());
    }

    @GetMapping("/degrade_rules")
    public Mono<List<DegradeRule>> degradeRules() {
        return Mono.just(DegradeRuleManager.getRules());
    }

    @GetMapping("/authority_rules")
    public Mono<List<AuthorityRule>> authorityRules() {
        return Mono.just(AuthorityRuleManager.getRules());
    }

    @GetMapping("/system_rules")
    public Mono<List<SystemRule>> systemRules() {
        return Mono.just(SystemRuleManager.getRules());
    }
}
