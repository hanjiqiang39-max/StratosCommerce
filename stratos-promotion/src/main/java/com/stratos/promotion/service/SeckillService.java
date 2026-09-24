package com.stratos.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.promotion.dto.SeckillDTO;
import com.stratos.promotion.entity.PromotionSeckill;
import com.stratos.promotion.entity.PromotionSeckillRecord;
import com.stratos.promotion.vo.SeckillResultVO;

/**
 * 秒杀服务接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface SeckillService extends IService<PromotionSeckill> {

    /**
     * 参与秒杀。已抢过则复用原记录，便于继续下单或看订单。
     */
    SeckillResultVO doSeckill(SeckillDTO dto);

    void warmup(Long seckillId);

    /**
     * 查询秒杀活动详情
     */
    PromotionSeckill querySeckillDetail(Long seckillId);

    /**
     * 进行中的秒杀活动
     */
    java.util.List<PromotionSeckill> listActive();

    void bindOrder(com.stratos.promotion.dto.BindSeckillOrderDTO dto);

    java.util.List<PromotionSeckillRecord> listUserRecords(Long userId);

    PromotionSeckillRecord findUserRecord(Long seckillId, Long userId);

    PromotionSeckillRecord findByOrderId(Long orderId);

}
