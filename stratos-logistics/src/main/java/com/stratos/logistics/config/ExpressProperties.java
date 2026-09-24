package com.stratos.logistics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 快递查询配置。填了 key 走快递100，否则 mock。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "logistics.express")
public class ExpressProperties {

    /**
     * mock | kuaidi100
     */
    private String vendor = "mock";

    private String customer = "";

    private String key = "";

    private String queryUrl = "https://poll.kuaidi100.com/poll/query.do";

    public boolean kuaidi100Ready() {
        return "kuaidi100".equalsIgnoreCase(vendor)
                && StringUtils.hasText(customer)
                && StringUtils.hasText(key);
    }
}
