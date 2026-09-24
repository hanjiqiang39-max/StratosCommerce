package com.stratos.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * 网关限流：秒杀下单与创建订单。
 */
@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void initRules() {
        Set<ApiDefinition> apis = new HashSet<>();
        ApiDefinition seckill = new ApiDefinition("seckill-do");
        seckill.setPredicateItems(Set.of(new ApiPathPredicateItem()
                .setPattern("/api/seckill/do")
                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT)));
        ApiDefinition orderCreate = new ApiDefinition("order-create");
        orderCreate.setPredicateItems(Set.of(new ApiPathPredicateItem()
                .setPattern("/api/order/create")
                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT)));
        ApiDefinition checkout = new ApiDefinition("order-checkout");
        checkout.setPredicateItems(Set.of(new ApiPathPredicateItem()
                .setPattern("/api/order/create-from-cart")
                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT)));
        apis.add(seckill);
        apis.add(orderCreate);
        apis.add(checkout);
        GatewayApiDefinitionManager.loadApiDefinitions(apis);

        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(flow("seckill-do", 20));
        rules.add(flow("order-create", 50));
        rules.add(flow("order-checkout", 50));
        GatewayRuleManager.loadRules(rules);
    }

    private GatewayFlowRule flow(String apiName, double qps) {
        GatewayFlowRule rule = new GatewayFlowRule(apiName);
        rule.setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME);
        rule.setCount(qps);
        rule.setIntervalSec(1);
        return rule;
    }
}
