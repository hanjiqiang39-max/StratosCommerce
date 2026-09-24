package com.stratos.logistics.express;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 本机 mock：不调外部快递 API
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
public class MockExpressClient implements ExpressClient {

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public List<ExpressTrace> query(String companyCode, String logisticsNo) {
        log.info("[EXPRESS MOCK] company={} no={}", companyCode, logisticsNo);
        return List.of();
    }
}
