package com.stratos.logistics.express;

import java.util.List;

/**
 * 快递查询渠道
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface ExpressClient {

    boolean isLive();

    List<ExpressTrace> query(String companyCode, String logisticsNo);
}
