package com.stratos.order.service;

import com.stratos.common.constant.CacheConstants;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.mq.OrderCreatedMessage;
import com.stratos.common.result.Result;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.order.dto.CreateOrderDTO;
import com.stratos.order.dto.LockStockCommand;
import com.stratos.order.entity.OrderInfo;
import com.stratos.order.entity.OrderItem;
import com.stratos.order.feign.InventoryFeignClient;
import com.stratos.order.mapper.OrderInfoMapper;
import com.stratos.order.mapper.OrderItemMapper;
import com.stratos.order.mq.OrderEventProducer;
import com.stratos.order.vo.OrderCreateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 订单落库 + 库存锁定。
 * 订单表是 ShardingSphere 逻辑表（order_info_0~7），Seata AT 拉不到主键元数据，
 * 不能用 @GlobalTransactional，改为本地事务：锁库存失败则回滚订单。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPersistService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final InventoryFeignClient inventoryFeignClient;
    private final OrderEventProducer orderEventProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO persist(CreateOrderDTO dto, BigDecimal totalAmount, BigDecimal freight,
                                 BigDecimal discount, BigDecimal couponAmount, BigDecimal pointsAmount) {
        String orderNo = generateOrderNo();
        BigDecimal safeDiscount = discount == null ? BigDecimal.ZERO : discount;
        BigDecimal safeCoupon = couponAmount == null ? BigDecimal.ZERO : couponAmount;
        BigDecimal safePoints = pointsAmount == null ? BigDecimal.ZERO : pointsAmount;
        BigDecimal payAmount = totalAmount.add(freight).subtract(safeDiscount).subtract(safeCoupon).subtract(safePoints);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        OrderInfo orderInfo = new OrderInfo();
        Long orderId = snowflakeIdGenerator.nextId();
        orderInfo.setId(orderId);
        orderInfo.setOrderNo(orderNo);
        orderInfo.setUserId(dto.getUserId());
        orderInfo.setShopId(resolveShopId(dto));
        orderInfo.setStatus(0);
        orderInfo.setTotalAmount(totalAmount);
        orderInfo.setFreightAmount(freight);
        orderInfo.setPayAmount(payAmount);
        orderInfo.setDiscountAmount(safeDiscount.add(safeCoupon));
        orderInfo.setCouponAmount(safeCoupon);
        orderInfo.setPointsAmount(safePoints);
        orderInfo.setUsePoints(dto.getUsePoints() != null ? dto.getUsePoints() : 0);
        orderInfo.setGainPoints(0);
        orderInfo.setDeliveryType(1);
        orderInfo.setAutoConfirmDay(7);
        orderInfo.setBuyerRemark(dto.getBuyerRemark());
        orderInfo.setCouponId(dto.getCouponId());
        if (!StringUtils.hasText(dto.getReceiverName())
                || !StringUtils.hasText(dto.getReceiverPhone())
                || !StringUtils.hasText(dto.getReceiverDetailAddress())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "请完善收货地址");
        }
        orderInfo.setReceiverName(dto.getReceiverName());
        orderInfo.setReceiverPhone(dto.getReceiverPhone());
        orderInfo.setReceiverProvince(blankToDefault(dto.getReceiverProvince(), "未知"));
        orderInfo.setReceiverCity(blankToDefault(dto.getReceiverCity(), "未知"));
        orderInfo.setReceiverDistrict(blankToDefault(dto.getReceiverDistrict(), "未知"));
        orderInfo.setReceiverDetailAddress(dto.getReceiverDetailAddress());
        LocalDateTime now = LocalDateTime.now();
        orderInfo.setCreateTime(now);
        orderInfo.setUpdateTime(now);
        orderInfoMapper.insert(orderInfo);

        for (CreateOrderDTO.OrderItemDTO itemDTO : dto.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(snowflakeIdGenerator.nextId());
            orderItem.setOrderId(orderId);
            orderItem.setOrderNo(orderNo);
            orderItem.setUserId(dto.getUserId());
            BigDecimal price = itemDTO.getPrice() == null ? BigDecimal.ZERO : itemDTO.getPrice();
            orderItem.setSpuId(itemDTO.getSpuId() != null ? itemDTO.getSpuId() : 0L);
            orderItem.setSkuId(itemDTO.getSkuId());
            orderItem.setSkuName(itemDTO.getSkuName() != null ? itemDTO.getSkuName() : "商品");
            orderItem.setSkuImage(itemDTO.getSkuImage());
            orderItem.setSkuCode(itemDTO.getSkuCode() != null ? itemDTO.getSkuCode() : "SKU" + itemDTO.getSkuId());
            orderItem.setPrice(price);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setTotalAmount(price.multiply(new BigDecimal(itemDTO.getQuantity())));
            orderItem.setDiscountAmount(BigDecimal.ZERO);
            orderItem.setRealAmount(orderItem.getTotalAmount());
            orderItem.setPromotionId(dto.getGroupBuyingId() != null ? dto.getGroupBuyingId() : dto.getFullDiscountId());
            orderItem.setCreateTime(now);
            orderItem.setUpdateTime(now);
            orderItemMapper.insert(orderItem);

            LockStockCommand lock = new LockStockCommand();
            lock.setUserId(dto.getUserId());
            lock.setOrderId(orderId);
            lock.setOrderNo(orderNo);
            lock.setWarehouseId(dto.getWarehouseId() != null ? dto.getWarehouseId() : 1L);
            lock.setProductId(itemDTO.getSpuId() != null ? itemDTO.getSpuId() : itemDTO.getSkuId());
            lock.setSkuId(itemDTO.getSkuId());
            lock.setQuantity(itemDTO.getQuantity());
            Result<Void> lockResult = inventoryFeignClient.lockStock(lock);
            if (lockResult == null || !lockResult.isSuccess()) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR,
                        lockResult == null ? "锁定库存失败" : lockResult.getMessage());
            }
        }

        return toCreateVo(orderInfo);
    }

    public void afterPersist(OrderCreateVO vo) {
        redisTemplate.opsForValue().set(
                CacheConstants.Order.ORDER_INFO + vo.getOrderId(),
                vo.getOrderNo(),
                CacheConstants.Expire.THIRTY_MINUTES,
                TimeUnit.SECONDS
        );
        orderEventProducer.sendCreated(new OrderCreatedMessage(
                vo.getOrderId(), vo.getOrderNo(), vo.getUserId(), vo.getPayAmount()));
    }

    private OrderCreateVO toCreateVo(OrderInfo orderInfo) {
        OrderCreateVO vo = new OrderCreateVO();
        vo.setOrderId(orderInfo.getId());
        vo.setOrderNo(orderInfo.getOrderNo());
        vo.setUserId(orderInfo.getUserId());
        vo.setTotalAmount(orderInfo.getTotalAmount());
        vo.setFreightAmount(orderInfo.getFreightAmount());
        vo.setPayAmount(orderInfo.getPayAmount());
        vo.setStatus(orderInfo.getStatus());
        return vo;
    }

    private Long resolveShopId(CreateOrderDTO dto) {
        if (dto.getShopId() != null) {
            return dto.getShopId();
        }
        if (dto.getItems() != null) {
            for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
                if (item.getShopId() != null) {
                    return item.getShopId();
                }
            }
        }
        return 1L;
    }

    private String blankToDefault(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", (int) (Math.random() * 1000000));
        return "SO" + timestamp + random;
    }
}
