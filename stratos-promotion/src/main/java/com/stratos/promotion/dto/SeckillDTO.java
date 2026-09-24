package com.stratos.promotion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 秒杀请求DTO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class SeckillDTO {

    @NotNull(message = "秒杀活动ID不能为空")
    private Long seckillId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

}
