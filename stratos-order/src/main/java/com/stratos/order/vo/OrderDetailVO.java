package com.stratos.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class OrderDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long userId;

    private Integer status;

    private String statusDesc;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private BigDecimal freightAmount;

    private BigDecimal discountAmount;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    private List<OrderItemVO> items;

    @Data
    public static class OrderItemVO implements Serializable {
        private Long id;
        private String skuName;
        private String skuImage;
        private String specDesc;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal realAmount;
    }

}
