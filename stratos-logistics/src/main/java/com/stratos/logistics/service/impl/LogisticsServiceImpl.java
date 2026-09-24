package com.stratos.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.logistics.dto.CreateShipmentDTO;
import com.stratos.logistics.dto.FreightQuoteDTO;
import com.stratos.logistics.entity.LogisticsCompany;
import com.stratos.logistics.entity.LogisticsFreightRegion;
import com.stratos.logistics.entity.LogisticsFreightTemplate;
import com.stratos.logistics.entity.LogisticsTrace;
import com.stratos.logistics.mapper.LogisticsCompanyMapper;
import com.stratos.logistics.mapper.LogisticsFreightRegionMapper;
import com.stratos.logistics.mapper.LogisticsFreightTemplateMapper;
import com.stratos.logistics.express.ExpressClient;
import com.stratos.logistics.express.ExpressTrace;
import com.stratos.logistics.mapper.LogisticsTraceMapper;
import com.stratos.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发货与轨迹（本机用模拟轨迹，不对接真实快递公司）。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsServiceImpl extends ServiceImpl<LogisticsTraceMapper, LogisticsTrace> implements LogisticsService {

    private static final String[] MOCK_STEPS = {
            "已揽收",
            "运输中",
            "到达目的城市",
            "派送中",
            "已签收"
    };

    private final LogisticsCompanyMapper logisticsCompanyMapper;
    private final LogisticsFreightTemplateMapper freightTemplateMapper;
    private final LogisticsFreightRegionMapper freightRegionMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final ExpressClient expressClient;

    @Override
    public List<LogisticsCompany> listCompanies() {
        return logisticsCompanyMapper.selectList(new LambdaQueryWrapper<LogisticsCompany>()
                .eq(LogisticsCompany::getStatus, 1)
                .orderByAsc(LogisticsCompany::getSortOrder));
    }

    @Override
    public List<LogisticsCompany> adminListCompanies() {
        return logisticsCompanyMapper.selectList(new LambdaQueryWrapper<LogisticsCompany>()
                .orderByAsc(LogisticsCompany::getSortOrder)
                .orderByDesc(LogisticsCompany::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveCompany(LogisticsCompany company) {
        if (company.getId() == null) {
            company.setId(snowflakeIdGenerator.nextId());
            if (company.getStatus() == null) {
                company.setStatus(1);
            }
            if (company.getSortOrder() == null) {
                company.setSortOrder(0);
            }
            logisticsCompanyMapper.insert(company);
        } else {
            logisticsCompanyMapper.updateById(company);
        }
        return company.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCompany(Long id) {
        logisticsCompanyMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createShipment(CreateShipmentDTO dto) {
        long exists = lambdaQuery().eq(LogisticsTrace::getOrderNo, dto.getOrderNo()).count();
        if (exists > 0) {
            LogisticsTrace first = lambdaQuery()
                    .eq(LogisticsTrace::getOrderNo, dto.getOrderNo())
                    .orderByAsc(LogisticsTrace::getTraceTime)
                    .last("LIMIT 1")
                    .one();
            return first.getLogisticsNo();
        }
        String companyCode = StringUtils.hasText(dto.getCompanyCode()) ? dto.getCompanyCode() : "SF";
        String logisticsNo = companyCode + System.currentTimeMillis();
        insertTrace(dto.getOrderId(), dto.getOrderNo(), companyCode, logisticsNo,
                LocalDateTime.now(), "GOT", MOCK_STEPS[0], "发货仓", "system");
        log.info("已发货 orderNo={} logisticsNo={}", dto.getOrderNo(), logisticsNo);
        return logisticsNo;
    }

    @Override
    public List<LogisticsTrace> queryTraceByOrderNo(String orderNo) {
        return lambdaQuery()
                .eq(LogisticsTrace::getOrderNo, orderNo)
                .orderByAsc(LogisticsTrace::getTraceTime)
                .list();
    }

    @Override
    public List<LogisticsTrace> queryTraceByLogisticsNo(String logisticsNo) {
        return lambdaQuery()
                .eq(LogisticsTrace::getLogisticsNo, logisticsNo)
                .orderByAsc(LogisticsTrace::getTraceTime)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncTrace(String logisticsNo) {
        List<LogisticsTrace> traces = queryTraceByLogisticsNo(logisticsNo);
        if (traces.isEmpty()) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "物流单不存在");
        }
        LogisticsTrace last = traces.get(traces.size() - 1);
        if (expressClient.isLive()) {
            List<ExpressTrace> remote = expressClient.query(last.getLogisticsCompanyCode(), logisticsNo);
            for (ExpressTrace step : remote) {
                boolean exists = traces.stream().anyMatch(item ->
                        StringUtils.hasText(item.getTraceDesc()) && item.getTraceDesc().equals(step.getDesc()));
                if (exists) {
                    continue;
                }
                insertTrace(last.getOrderId(), last.getOrderNo(), last.getLogisticsCompanyCode(), logisticsNo,
                        step.getTime() == null ? LocalDateTime.now() : step.getTime(),
                        step.getStatus(), step.getDesc(), step.getLocation(), "kuaidi100");
            }
            return;
        }
        if (traces.size() >= MOCK_STEPS.length) {
            return;
        }
        insertTrace(last.getOrderId(), last.getOrderNo(), last.getLogisticsCompanyCode(), logisticsNo,
                LocalDateTime.now(), "TRANSIT", MOCK_STEPS[traces.size()], "在途", "system");
    }

    private void insertTrace(Long orderId, String orderNo, String companyCode, String logisticsNo,
                             LocalDateTime time, String status, String desc, String location, String operator) {
        LogisticsTrace trace = new LogisticsTrace();
        trace.setId(snowflakeIdGenerator.nextId());
        trace.setOrderId(orderId);
        trace.setOrderNo(orderNo);
        trace.setLogisticsCompanyCode(companyCode);
        trace.setLogisticsNo(logisticsNo);
        trace.setTraceTime(time);
        trace.setTraceStatus(status);
        trace.setTraceDesc(desc);
        trace.setTraceLocation(location);
        trace.setOperator(operator);
        trace.setCreateTime(time);
        save(trace);
    }

    @Override
    public BigDecimal quoteFreight(FreightQuoteDTO dto) {
        Long templateId = dto.getTemplateId() == null ? 1L : dto.getTemplateId();
        LogisticsFreightTemplate template = freightTemplateMapper.selectById(templateId);
        if (template == null || (template.getStatus() != null && template.getStatus() == 0)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "运费模板不存在");
        }
        BigDecimal orderAmount = dto.getOrderAmount() == null ? BigDecimal.ZERO : dto.getOrderAmount();
        if (template.getIsFreeShipping() != null && template.getIsFreeShipping() == 1) {
            return BigDecimal.ZERO;
        }
        if (template.getFreeShippingAmount() != null
                && template.getFreeShippingAmount().compareTo(BigDecimal.ZERO) > 0
                && orderAmount.compareTo(template.getFreeShippingAmount()) >= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal firstUnit = template.getDefaultFirstUnit();
        BigDecimal firstFee = template.getDefaultFirstFee();
        BigDecimal continueUnit = template.getDefaultContinueUnit();
        BigDecimal continueFee = template.getDefaultContinueFee();
        if (StringUtils.hasText(dto.getProvince())) {
            List<LogisticsFreightRegion> regions = freightRegionMapper.selectList(
                    new LambdaQueryWrapper<LogisticsFreightRegion>()
                            .eq(LogisticsFreightRegion::getTemplateId, templateId));
            for (LogisticsFreightRegion region : regions) {
                if (StringUtils.hasText(region.getRegionCodes())
                        && region.getRegionCodes().contains(dto.getProvince())) {
                    firstUnit = region.getFirstUnit();
                    firstFee = region.getFirstFee();
                    continueUnit = region.getContinueUnit();
                    continueFee = region.getContinueFee();
                    break;
                }
            }
        }
        BigDecimal units = dto.getQuantity() == null ? BigDecimal.ONE : new BigDecimal(dto.getQuantity());
        if (template.getChargeType() != null && template.getChargeType() == 2 && dto.getWeight() != null) {
            units = dto.getWeight();
        }
        if (firstUnit == null || firstFee == null) {
            return BigDecimal.ZERO;
        }
        if (units.compareTo(firstUnit) <= 0) {
            return firstFee.setScale(2, RoundingMode.HALF_UP);
        }
        if (continueUnit == null || continueUnit.compareTo(BigDecimal.ZERO) <= 0 || continueFee == null) {
            return firstFee.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal extra = units.subtract(firstUnit);
        BigDecimal steps = extra.divide(continueUnit, 0, RoundingMode.UP);
        return firstFee.add(steps.multiply(continueFee)).setScale(2, RoundingMode.HALF_UP);
    }

}
