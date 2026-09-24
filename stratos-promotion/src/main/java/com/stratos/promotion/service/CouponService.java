package com.stratos.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.promotion.dto.ReceiveCouponDTO;
import com.stratos.promotion.entity.PromotionCoupon;
import com.stratos.promotion.entity.UserCoupon;
import com.stratos.promotion.vo.UserCouponVO;

import java.util.List;

/**
 * 优惠券服务接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface CouponService extends IService<UserCoupon> {

    /**
     * 领取优惠券
     */
    void receiveCoupon(ReceiveCouponDTO dto);

    /**
     * 查询用户优惠券
     */
    List<UserCoupon> queryUserCoupons(Long userId, Integer status);

    /**
     * 查询用户优惠券（带模板名称和面额）
     */
    List<UserCouponVO> queryUserCouponVOs(Long userId, Integer status);

    /**
     * 使用优惠券
     */
    void useCoupon(Long userCouponId, Long orderId, String orderNo);

    /**
     * 可领取优惠券模板
     */
    List<PromotionCoupon> listAvailableCoupons();

    /**
     * 试算用户券抵扣。userCouponId 为用户券主键。
     */
    java.math.BigDecimal quote(Long userCouponId, Long userId, java.math.BigDecimal amount);

}
