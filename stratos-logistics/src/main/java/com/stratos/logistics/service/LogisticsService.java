package com.stratos.logistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.logistics.dto.CreateShipmentDTO;
import com.stratos.logistics.dto.FreightQuoteDTO;
import com.stratos.logistics.entity.LogisticsCompany;
import com.stratos.logistics.entity.LogisticsTrace;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物流服务
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface LogisticsService extends IService<LogisticsTrace> {

    List<LogisticsCompany> listCompanies();

    List<LogisticsCompany> adminListCompanies();

    Long saveCompany(LogisticsCompany company);

    void deleteCompany(Long id);

    String createShipment(CreateShipmentDTO dto);

    List<LogisticsTrace> queryTraceByOrderNo(String orderNo);

    List<LogisticsTrace> queryTraceByLogisticsNo(String logisticsNo);

    void syncTrace(String logisticsNo);

    BigDecimal quoteFreight(FreightQuoteDTO dto);

}
