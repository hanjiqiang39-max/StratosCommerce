package com.stratos.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.promotion.dto.FullDiscountCalcDTO;
import com.stratos.promotion.dto.FullDiscountCalcVO;
import com.stratos.promotion.entity.PromotionFullDiscount;
import com.stratos.promotion.mapper.PromotionFullDiscountMapper;
import com.stratos.promotion.service.FullDiscountService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FullDiscountServiceImpl extends ServiceImpl<PromotionFullDiscountMapper, PromotionFullDiscount>
        implements FullDiscountService {

    @Override
    public List<PromotionFullDiscount> listActive() {
        LocalDateTime now = LocalDateTime.now();
        return lambdaQuery()
                .eq(PromotionFullDiscount::getStatus, 1)
                .le(PromotionFullDiscount::getStartTime, now)
                .ge(PromotionFullDiscount::getEndTime, now)
                .orderByDesc(PromotionFullDiscount::getSortOrder)
                .orderByDesc(PromotionFullDiscount::getFullAmount)
                .list();
    }

    @Override
    public FullDiscountCalcVO calculate(FullDiscountCalcDTO dto) {
        BigDecimal amount = dto.getAmount() == null ? BigDecimal.ZERO : dto.getAmount();
        PromotionFullDiscount matched = null;
        if (dto.getActivityId() != null) {
            PromotionFullDiscount specified = getById(dto.getActivityId());
            if (specified != null && eligible(specified, amount, dto.getSpuIds())) {
                matched = specified;
            }
        }
        if (matched == null) {
            for (PromotionFullDiscount activity : listActive()) {
                if (eligible(activity, amount, dto.getSpuIds())) {
                    matched = activity;
                    break;
                }
            }
        }
        FullDiscountCalcVO vo = new FullDiscountCalcVO();
        vo.setDiscountAmount(BigDecimal.ZERO);
        vo.setPayAmount(amount);
        if (matched == null) {
            return vo;
        }
        vo.setActivityId(matched.getId());
        vo.setActivityName(displayName(matched));
        vo.setFullAmount(matched.getFullAmount());
        vo.setDiscountAmount(matched.getDiscountAmount());
        BigDecimal pay = amount.subtract(matched.getDiscountAmount());
        vo.setPayAmount(pay.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pay);
        return vo;
    }

    private String displayName(PromotionFullDiscount activity) {
        String name = activity.getActivityName() == null ? "" : activity.getActivityName().trim();
        if (!name.isEmpty() && !name.contains("?") && !name.contains("？") && !name.contains("�")) {
            return name;
        }
        BigDecimal full = activity.getFullAmount() == null ? BigDecimal.ZERO : activity.getFullAmount();
        BigDecimal off = activity.getDiscountAmount() == null ? BigDecimal.ZERO : activity.getDiscountAmount();
        if (full.compareTo(BigDecimal.ZERO) > 0 && off.compareTo(BigDecimal.ZERO) > 0) {
            return "满" + full.stripTrailingZeros().toPlainString() + "减" + off.stripTrailingZeros().toPlainString();
        }
        return "满减";
    }

    private boolean eligible(PromotionFullDiscount activity, BigDecimal amount, List<Long> spuIds) {
        if (activity.getStatus() == null || activity.getStatus() != 1) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            return false;
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            return false;
        }
        if (activity.getFullAmount() == null || amount.compareTo(activity.getFullAmount()) < 0) {
            return false;
        }
        if (activity.getUseType() != null && activity.getUseType() == 3 && StringUtils.hasText(activity.getSpuIds())) {
            if (spuIds == null || spuIds.isEmpty()) {
                return false;
            }
            for (Long spuId : spuIds) {
                if (activity.getSpuIds().contains(String.valueOf(spuId))) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
