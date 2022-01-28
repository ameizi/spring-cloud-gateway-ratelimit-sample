package com.anchnet.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.redis.RedisDataSource;
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;
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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class RedisDataSourceConfig implements ApplicationRunner {

    @Value("${spring.redis.host}")
    public String redisHost;

    @Value("${spring.redis.port}")
    public int redisPort;

    @Value("${spring.redis.database}")
    public int database;

    @Value("${spring.redis.password}")
    public String redisPass;

    /**
     * Gateway ApiDefinition
     */
    public static final String GATEWAY_API_DEFINITION = "gateway_sentinel_api_definition";
    public static final String GATEWAY_API_DEFINITION_CHANNEL = "gateway_sentinel_api_definition_channel";

    /**
     * Gateway限流规则key前缀
     */
    public static final String GATEWAY_RULE_FLOW = "gateway_sentinel_rule_flow";
    public static final String GATEWAY_RULE_FLOW_CHANNEL = "gateway_sentinel_rule_flow_channel";

    /**
     * 限流规则key前缀
     */
    public static final String RULE_FLOW = "sentinel_rule_flow";
    public static final String RULE_FLOW_CHANNEL = "sentinel_rule_flow_channel";

    /**
     * 降级规则key前缀
     */
    public static final String RULE_DEGRADE = "sentinel_rule_degrade";
    public static final String RULE_DEGRADE_CHANNEL = "sentinel_rule_degrade_channel";

    /**
     * 系统规则key前缀
     */
    public static final String RULE_SYSTEM = "sentinel_rule_system";
    public static final String RULE_SYSTEM_CHANNEL = "sentinel_rule_system_channel";

    /**
     * 参数热点规则key前缀
     */
    public static final String RULE_PARAM = "sentinel_rule_param";
    public static final String RULE_PARAM_CHANNEL = "sentinel_rule_param_channel";

    /**
     * 授权规则key前缀
     */
    public static final String RULE_AUTHORITY = "sentinel_rule_authority";
    public static final String RULE_AUTHORITY_CHANNEL = "sentinel_rule_authority_channel";

    /**
     * 集群限流key前缀
     */
    public static final String RULE_CLUSTER = "sentinel_cluster_rule_flow";
    public static final String RULE_CLUSTER_MAP = "sentinel_cluster_rule_flow_map";

    public static final String RULE_CLUSTER_CLIENT_CONFIG = "sentinel_cluster_client_config";
    public static final String RULE_CLUSTER_CLIENT_CONFIG_CHANNEL = "sentinel_cluster_client_config_channel";


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info(">>>>>>>>>执行sentinel规则初始化 start。。。");
        RedisConnectionConfig config = RedisConnectionConfig.builder().withDatabase(database).withHost(redisHost).withPort(redisPort).withPassword(redisPass).build();

        // Gateway ApiDefinition
        Converter<String, Set<ApiDefinition>> parserGatewayApiDefinition = this::parseJson;
        ReadableDataSource<String, Set<ApiDefinition>> redisDataSourceGatewayApiDefinition = new RedisDataSource<>(config, SentinelConfig.getAppName() + ":" + GATEWAY_API_DEFINITION, GATEWAY_API_DEFINITION_CHANNEL, parserGatewayApiDefinition);
        GatewayApiDefinitionManager.register2Property(redisDataSourceGatewayApiDefinition.getProperty());

        // Gateway流控规则
        Converter<String, Set<GatewayFlowRule>> parserGatewayFlow = source -> JSON.parseObject(source, new TypeReference<Set<GatewayFlowRule>>() {
        });
        ReadableDataSource<String, Set<GatewayFlowRule>> redisDataSourceGatewayFlow = new RedisDataSource<>(config, SentinelConfig.getAppName() + ":" + GATEWAY_RULE_FLOW, GATEWAY_RULE_FLOW_CHANNEL, parserGatewayFlow);
        GatewayRuleManager.register2Property(redisDataSourceGatewayFlow.getProperty());

        // 流控规则
        Converter<String, List<FlowRule>> parserFlow = source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
        });
        ReadableDataSource<String, List<FlowRule>> redisDataSourceFlow = new RedisDataSource<>(config, SentinelConfig.getAppName() + ":" + RULE_FLOW, RULE_FLOW_CHANNEL, parserFlow);
        FlowRuleManager.register2Property(redisDataSourceFlow.getProperty());

        // 降级规则
        Converter<String, List<DegradeRule>> parserDegrade = source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {
        });
        ReadableDataSource<String, List<DegradeRule>> redisDataSourceDegrade = new RedisDataSource<>(config, SentinelConfig.getAppName() + ":" + RULE_DEGRADE, RULE_DEGRADE_CHANNEL, parserDegrade);
        DegradeRuleManager.register2Property(redisDataSourceDegrade.getProperty());

        // 热点规则
        Converter<String, List<ParamFlowRule>> parserParam = source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {
        });
        ReadableDataSource<String, List<ParamFlowRule>> redisDataSourceParam = new RedisDataSource<>(config, SentinelConfig.getAppName() + ":" + RULE_PARAM, RULE_PARAM_CHANNEL, parserParam);
        ParamFlowRuleManager.register2Property(redisDataSourceParam.getProperty());

        // 系统规则
        Converter<String, List<SystemRule>> parserSystem = source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {
        });
        ReadableDataSource<String, List<SystemRule>> redisDataSourceSystem = new RedisDataSource<>(config, SentinelConfig.getAppName() + ":" + RULE_SYSTEM, RULE_SYSTEM_CHANNEL, parserSystem);
        SystemRuleManager.register2Property(redisDataSourceSystem.getProperty());

        // 授权规则
        Converter<String, List<AuthorityRule>> parserAuthority = source -> JSON.parseObject(source, new TypeReference<List<AuthorityRule>>() {
        });
        ReadableDataSource<String, List<AuthorityRule>> redisDataSourceAuthority = new RedisDataSource<>(config, SentinelConfig.getAppName() + ":" + RULE_AUTHORITY, RULE_AUTHORITY_CHANNEL, parserAuthority);
        AuthorityRuleManager.register2Property(redisDataSourceAuthority.getProperty());
        log.info(">>>>>>>>>执行sentinel规则初始化 end。。。");
    }

    private Set<ApiDefinition> parseJson(String data) {
        Set<ApiDefinition> apiDefinitions = new HashSet<>();
        JSONArray array = JSON.parseArray(data);
        for (Object obj : array) {
            JSONObject o = (JSONObject) obj;
            ApiDefinition apiDefinition = new ApiDefinition((o.getString("apiName")));
            Set<ApiPredicateItem> predicateItems = new HashSet<>();
            JSONArray itemArray = o.getJSONArray("predicateItems");
            if (itemArray != null) {
                predicateItems.addAll(itemArray.toJavaList(ApiPathPredicateItem.class));
            }
            apiDefinition.setPredicateItems(predicateItems);
            apiDefinitions.add(apiDefinition);
        }
        return apiDefinitions;
    }

}
