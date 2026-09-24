package com.stratos.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.common.result.ResultCode;
import com.stratos.common.exception.BusinessException;
import com.stratos.promotion.dto.ReceiveCouponDTO;
import com.stratos.promotion.entity.PromotionCoupon;
import com.stratos.promotion.entity.UserCoupon;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.promotion.mapper.PromotionCouponMapper;
import com.stratos.promotion.mapper.UserCouponMapper;
import com.stratos.promotion.service.CouponService;
import com.stratos.promotion.vo.UserCouponVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 优惠券服务实现
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements CouponService {

    private final PromotionCouponMapper couponMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveCoupon(ReceiveCouponDTO dto) {
        PromotionCoupon coupon = couponMapper.selectById(dto.getCouponId());
        if (coupon == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "优惠券不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "优惠券未到领取时间");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "优惠券已过期");
        }

        int received = coupon.getReceivedCount() == null ? 0 : coupon.getReceivedCount();
        if (coupon.getPublishCount() != null && coupon.getPublishCount() != -1 && received >= coupon.getPublishCount()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "优惠券已抢光");
        }

        // 检查用户已领取数量
        long receivedCount = lambdaQuery()
                .eq(UserCoupon::getUserId, dto.getUserId())
                .eq(UserCoupon::getCouponId, dto.getCouponId())
                .count();

        if (receivedCount >= coupon.getLimitPerUser()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "已达到领取上限");
        }

        int rows = couponMapper.update(null, new LambdaUpdateWrapper<PromotionCoupon>()
                .setSql("received_count = received_count + 1")
                .eq(PromotionCoupon::getId, coupon.getId())
                .eq(coupon.getPublishCount() != null && coupon.getPublishCount() != -1,
                        PromotionCoupon::getReceivedCount, received));
        if (rows == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "优惠券已抢光");
        }

        int validDays = coupon.getValidDays() != null && coupon.getValidDays() > 0 ? coupon.getValidDays() : 7;
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setId(snowflakeIdGenerator.nextId());
        userCoupon.setUserId(dto.getUserId());
        userCoupon.setCouponId(dto.getCouponId());
        userCoupon.setCouponCode(coupon.getCouponCode() != null ? coupon.getCouponCode() : UUID.randomUUID().toString().replace("-", ""));
        userCoupon.setReceiveType(1);
        userCoupon.setStatus(0);
        userCoupon.setReceiveTime(now);
        userCoupon.setStartTime(now);
        userCoupon.setEndTime(now.plusDays(validDays));
        userCoupon.setCreateTime(now);
        userCoupon.setUpdateTime(now);

        save(userCoupon);
    }

    @Override
    public List<UserCoupon> queryUserCoupons(Long userId, Integer status) {
        return lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(status != null, UserCoupon::getStatus, status)
                .orderByDesc(UserCoupon::getReceiveTime)
                .list();
    }

    @Override
    public List<UserCouponVO> queryUserCouponVOs(Long userId, Integer status) {
        return queryUserCoupons(userId, status).stream().map(item -> {
            UserCouponVO vo = new UserCouponVO();
            vo.setId(item.getId());
            vo.setCouponId(item.getCouponId());
            vo.setUserId(item.getUserId());
            vo.setCouponCode(item.getCouponCode());
            vo.setStatus(item.getStatus());
            vo.setReceiveTime(item.getReceiveTime());
            vo.setStartTime(item.getStartTime());
            vo.setEndTime(item.getEndTime());
            vo.setOrderId(item.getOrderId());
            vo.setOrderNo(item.getOrderNo());
            PromotionCoupon coupon = couponMapper.selectById(item.getCouponId());
            if (coupon != null) {
                vo.setCouponName(coupon.getCouponName());
                vo.setCouponType(coupon.getCouponType());
                vo.setDiscountType(coupon.getDiscountType());
                vo.setDiscountValue(coupon.getDiscountValue());
                vo.setMinAmount(coupon.getMinAmount());
                vo.setMaxDiscount(coupon.getMaxDiscount());
            }
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(Long userCouponId, Long orderId, String orderNo) {
        UserCoupon userCoupon = getById(userCouponId);
        if (userCoupon == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "优惠券不存在");
        }

        if (userCoupon.getStatus() != 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "优惠券状态不可用");
        }

        if (userCoupon.getEndTime() != null && LocalDateTime.now().isAfter(userCoupon.getEndTime())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "优惠券已过期");
        }

        userCoupon.setStatus(1);
        userCoupon.setUseTime(LocalDateTime.now());
        userCoupon.setOrderId(orderId);
        userCoupon.setOrderNo(orderNo);

        updateById(userCoupon);
    }

    @Override
    public java.math.BigDecimal quote(Long userCouponId, Long userId, java.math.BigDecimal amount) {
        if (userCouponId == null || amount == null) {
            return java.math.BigDecimal.ZERO;
        }
        UserCoupon userCoupon = getById(userCouponId);
        if (userCoupon == null || (userId != null && !userId.equals(userCoupon.getUserId()))) {
            throw new BusinessException(ResultCode.COUPON_NOT_AVAILABLE);
        }
        if (userCoupon.getStatus() == null || userCoupon.getStatus() != 0) {
            throw new BusinessException(ResultCode.COUPON_NOT_AVAILABLE);
        }
        if (userCoupon.getEndTime() != null && LocalDateTime.now().isAfter(userCoupon.getEndTime())) {
            throw new BusinessException(ResultCode.COUPON_EXPIRED);
        }
        PromotionCoupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new BusinessException(ResultCode.COUPON_NOT_AVAILABLE);
        }
        if (coupon.getMinAmount() != null && amount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException(ResultCode.COUPON_NOT_AVAILABLE, "未满优惠券使用门槛");
        }
        java.math.BigDecimal discount;
        if (coupon.getDiscountType() != null && coupon.getDiscountType() == 2) {
            java.math.BigDecimal rate = coupon.getDiscountValue() == null
                    ? java.math.BigDecimal.ZERO
                    : coupon.getDiscountValue();
            discount = amount.multiply(java.math.BigDecimal.ONE.subtract(
                    rate.divide(new java.math.BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP)));
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        } else {
            discount = coupon.getDiscountValue() == null ? java.math.BigDecimal.ZERO : coupon.getDiscountValue();
        }
        if (discount.compareTo(amount) > 0) {
            discount = amount;
        }
        return discount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public List<PromotionCoupon> listAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PromotionCoupon>()
                .eq(PromotionCoupon::getStatus, 1)
                .and(w -> w.isNull(PromotionCoupon::getStartTime).or().le(PromotionCoupon::getStartTime, now))
                .and(w -> w.isNull(PromotionCoupon::getEndTime).or().ge(PromotionCoupon::getEndTime, now))
                .orderByDesc(PromotionCoupon::getCreateTime));
    }

}
