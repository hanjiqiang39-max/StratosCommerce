package com.stratos.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.promotion.dto.FullDiscountCalcDTO;
import com.stratos.promotion.dto.FullDiscountCalcVO;
import com.stratos.promotion.entity.PromotionFullDiscount;

import java.util.List;

public interface FullDiscountService extends IService<PromotionFullDiscount> {

    List<PromotionFullDiscount> listActive();

    FullDiscountCalcVO calculate(FullDiscountCalcDTO dto);
}
