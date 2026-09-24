package com.stratos.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.common.constant.CacheConstants;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.mq.OrderCancelledMessage;
import com.stratos.common.result.Result;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.order.dto.CheckoutDTO;
import com.stratos.order.dto.CreateOrderDTO;
import com.stratos.order.dto.CreateShipmentCommand;
import com.stratos.order.dto.FreightQuoteCommand;
import com.stratos.order.dto.PaymentRefundCommand;
import com.stratos.order.dto.RefundApplyDTO;
import com.stratos.order.dto.RefundAuditDTO;
import com.stratos.order.entity.OrderInfo;
import com.stratos.order.entity.OrderItem;
import com.stratos.order.entity.OrderRefund;
import com.stratos.order.entity.ShoppingCart;
import com.stratos.order.feign.AccountFeignClient;
import com.stratos.order.feign.InventoryFeignClient;
import com.stratos.order.feign.LogisticsFeignClient;
import com.stratos.order.feign.NotificationFeignClient;
import com.stratos.order.feign.PaymentFeignClient;
import com.stratos.order.feign.ProductFeignClient;
import com.stratos.order.feign.PromotionFeignClient;
import com.stratos.order.feign.UserFeignClient;
import com.stratos.order.mapper.OrderInfoMapper;
import com.stratos.order.mapper.OrderItemMapper;
import com.stratos.order.mapper.OrderRefundMapper;
import com.stratos.order.mq.OrderEventProducer;
import com.stratos.order.vo.OrderCreateVO;
import com.stratos.order.vo.OrderDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 订单服务
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderRefundMapper orderRefundMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final InventoryFeignClient inventoryFeignClient;
    private final ProductFeignClient productFeignClient;
    private final UserFeignClient userFeignClient;
    private final LogisticsFeignClient logisticsFeignClient;
    private final PaymentFeignClient paymentFeignClient;
    private final NotificationFeignClient notificationFeignClient;
    private final PromotionFeignClient promotionFeignClient;
    private final AccountFeignClient accountFeignClient;
    private final CartService cartService;
    private final OrderEventProducer orderEventProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderPersistService orderPersistService;

    public OrderCreateVO createOrder(CreateOrderDTO dto) {
        fillSkuFromProduct(dto);
        fillAddressIfNeeded(dto);
        applyGroupPrice(dto);
        applySeckillPrice(dto);
        tagPromotionRemark(dto);
        BigDecimal amount = BigDecimal.ZERO;
        int quantity = 0;
        for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
            BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
            amount = amount.add(price.multiply(new BigDecimal(item.getQuantity())));
            quantity += item.getQuantity();
        }
        BigDecimal totalAmount = amount;
        BigDecimal freight = quoteFreight(dto, totalAmount, quantity);
        BigDecimal discount = calculateFullDiscount(dto, totalAmount);
        BigDecimal couponAmount = quoteCoupon(dto, totalAmount);
        BigDecimal afterCoupon = totalAmount.add(freight).subtract(discount).subtract(couponAmount);
        if (afterCoupon.compareTo(BigDecimal.ZERO) < 0) {
            afterCoupon = BigDecimal.ZERO;
        }
        BigDecimal pointsAmount = quotePoints(dto, afterCoupon);
        OrderCreateVO vo = orderPersistService.persist(dto, totalAmount, freight, discount, couponAmount, pointsAmount);
        orderPersistService.afterPersist(vo);
        consumeCoupon(dto, vo);
        consumePoints(dto, vo);
        bindGroupOrder(dto, vo.getOrderId(), vo.getOrderNo());
        bindSeckillOrder(dto, vo.getOrderId(), vo.getOrderNo());
        notifyUser(dto.getUserId(), "下单成功", "订单 " + vo.getOrderNo() + " 已创建，请尽快支付",
                "/order/" + vo.getOrderId());
        return vo;
    }

    public OrderCreateVO createFromCart(CheckoutDTO checkout) {
        List<ShoppingCart> carts;
        if (checkout.getCartIds() != null && !checkout.getCartIds().isEmpty()) {
            carts = cartService.list(checkout.getUserId()).stream()
                    .filter(item -> checkout.getCartIds().contains(item.getId()))
                    .toList();
        } else {
            carts = cartService.listSelected(checkout.getUserId());
        }
        if (carts.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "购物车没有可结算商品");
        }
        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setUserId(checkout.getUserId());
        dto.setAddressId(checkout.getAddressId());
        dto.setReceiverName(checkout.getReceiverName());
        dto.setReceiverPhone(checkout.getReceiverPhone());
        dto.setReceiverProvince(checkout.getReceiverProvince());
        dto.setReceiverCity(checkout.getReceiverCity());
        dto.setReceiverDistrict(checkout.getReceiverDistrict());
        dto.setReceiverDetailAddress(checkout.getReceiverDetailAddress());
        dto.setWarehouseId(checkout.getWarehouseId());
        dto.setCouponId(checkout.getCouponId());
        dto.setUsePoints(checkout.getUsePoints());
        dto.setFullDiscountId(checkout.getFullDiscountId());
        dto.setGroupBuyingId(checkout.getGroupBuyingId());
        dto.setGroupRecordId(checkout.getGroupRecordId());
        dto.setSeckillId(checkout.getSeckillId());
        dto.setSeckillRecordId(checkout.getSeckillRecordId());
        dto.setFreightTemplateId(checkout.getFreightTemplateId());
        dto.setBuyerRemark(checkout.getBuyerRemark());
        List<CreateOrderDTO.OrderItemDTO> items = new ArrayList<>();
        for (ShoppingCart cart : carts) {
            CreateOrderDTO.OrderItemDTO item = new CreateOrderDTO.OrderItemDTO();
            item.setSkuId(cart.getSkuId());
            item.setSpuId(cart.getSpuId());
            item.setQuantity(cart.getQuantity());
            items.add(item);
        }
        dto.setItems(items);
        OrderCreateVO vo = createOrder(dto);
        for (ShoppingCart cart : carts) {
            cartService.delete(cart.getId(), checkout.getUserId());
        }
        return vo;
    }

    public PageResult<OrderInfo> adminList(PageQuery pageQuery, Integer status, String orderNo) {
        return adminList(pageQuery, status, orderNo, null);
    }

    public PageResult<OrderInfo> adminList(PageQuery pageQuery, Integer status, String orderNo, Long shopId) {
        Page<OrderInfo> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(status != null, OrderInfo::getStatus, status);
        queryWrapper.eq(StringUtils.hasText(orderNo), OrderInfo::getOrderNo, orderNo);
        queryWrapper.eq(shopId != null, OrderInfo::getShopId, shopId);
        queryWrapper.orderByDesc(OrderInfo::getCreateTime);
        Page<OrderInfo> result = orderInfoMapper.selectPage(page, queryWrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(),
                pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSellerRemark(Long orderId, String remark) {
        updateSellerRemark(orderId, remark, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSellerRemark(Long orderId, String remark, Long shopId) {
        OrderInfo orderInfo = requireShopOrder(orderId, shopId);
        patchOrder(orderInfo, wrapper -> wrapper.set(OrderInfo::getSellerRemark, remark));
    }

    @Transactional(rollbackFor = Exception.class)
    public String ship(Long orderId, String companyCode) {
        return ship(orderId, companyCode, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public String ship(Long orderId, String companyCode, Long shopId) {
        OrderInfo orderInfo = requireShopOrder(orderId, shopId);
        if (orderInfo.getStatus() == null || orderInfo.getStatus() != 20) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "只有待发货订单可以发货");
        }
        CreateShipmentCommand command = new CreateShipmentCommand();
        command.setOrderId(orderInfo.getId());
        command.setOrderNo(orderInfo.getOrderNo());
        command.setCompanyCode(StringUtils.hasText(companyCode) ? companyCode : "SF");
        Result<String> remote = logisticsFeignClient.ship(command);
        if (remote == null || !remote.isSuccess()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    remote == null ? "物流发货失败" : remote.getMessage());
        }
        patchOrder(orderInfo, wrapper -> wrapper.set(OrderInfo::getStatus, 30));
        return remote.getData();
    }

    public PageResult<OrderInfo> getUserOrders(Long userId) {
        return getUserOrders(userId, new PageQuery());
    }

    public PageResult<OrderInfo> getUserOrders(Long userId, PageQuery pageQuery) {
        Page<OrderInfo> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getUserId, userId).orderByDesc(OrderInfo::getCreateTime);
        Page<OrderInfo> result = orderInfoMapper.selectPage(page, queryWrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(),
                pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    public OrderDetailVO getOrderDetail(Long orderId) {
        return toDetailVo(requireOrder(orderId));
    }

    public OrderDetailVO getOrderDetailByNo(String orderNo) {
        OrderInfo orderInfo = findOrderByNo(orderNo);
        if (orderInfo == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return toDetailVo(orderInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (orderInfo.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "只有待支付订单可以取消");
        }
        patchOrder(orderInfo, wrapper -> wrapper
                .set(OrderInfo::getStatus, -10)
                .set(OrderInfo::getCloseTime, LocalDateTime.now()));
        Result<Void> unlockResult = inventoryFeignClient.unlockStock(orderId);
        if (unlockResult == null || !unlockResult.isSuccess()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    unlockResult == null ? "释放库存失败" : unlockResult.getMessage());
        }
        redisTemplate.delete(CacheConstants.Order.ORDER_INFO + orderId);
        orderEventProducer.sendCancelled(new OrderCancelledMessage(orderId, orderInfo.getOrderNo(), orderInfo.getUserId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (orderInfo.getStatus() != null && orderInfo.getStatus() >= 10) {
            return;
        }
        if (orderInfo.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单不是待支付状态");
        }
        patchOrder(orderInfo, wrapper -> wrapper
                .set(OrderInfo::getStatus, 20)
                .set(OrderInfo::getPaymentTime, LocalDateTime.now()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (orderInfo.getStatus() != 30) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "只有已发货订单可以确认收货");
        }
        patchOrder(orderInfo, wrapper -> wrapper
                .set(OrderInfo::getStatus, 40)
                .set(OrderInfo::getConfirmTime, LocalDateTime.now()));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long applyRefund(RefundApplyDTO dto) {
        OrderInfo orderInfo = orderInfoMapper.selectById(dto.getOrderId());
        if (orderInfo == null || !orderInfo.getUserId().equals(dto.getUserId())) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (orderInfo.getStatus() < 10) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "当前订单不可申请售后");
        }
        OrderRefund refund = new OrderRefund();
        refund.setId(snowflakeIdGenerator.nextId());
        refund.setRefundNo("RF" + System.currentTimeMillis());
        refund.setOrderId(orderInfo.getId());
        refund.setOrderNo(orderInfo.getOrderNo());
        refund.setUserId(dto.getUserId());
        refund.setRefundType(dto.getRefundType());
        refund.setRefundReason(dto.getRefundReason());
        refund.setRefundDesc(dto.getRefundDesc());
        refund.setRefundAmount(dto.getRefundAmount());
        refund.setStatus(0);
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        orderRefundMapper.insert(refund);
        patchOrder(orderInfo, wrapper -> wrapper.set(OrderInfo::getStatus, -20));
        return refund.getId();
    }

    public List<OrderRefund> listRefunds(Long userId) {
        return orderRefundMapper.selectList(new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getUserId, userId)
                .orderByDesc(OrderRefund::getCreateTime));
    }

    public OrderRefund getRefund(Long refundId) {
        OrderRefund refund = orderRefundMapper.selectById(refundId);
        if (refund == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "售后单不存在");
        }
        return refund;
    }

    public List<OrderRefund> listRefundsByShop(Long shopId) {
        List<OrderInfo> orders = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getShopId, shopId));
        if (orders.isEmpty()) {
            return List.of();
        }
        List<Long> orderIds = orders.stream().map(OrderInfo::getId).toList();
        return orderRefundMapper.selectList(new LambdaQueryWrapper<OrderRefund>()
                .in(OrderRefund::getOrderId, orderIds)
                .orderByDesc(OrderRefund::getCreateTime));
    }

    public List<OrderRefund> listAllRefunds(Integer status) {
        return orderRefundMapper.selectList(new LambdaQueryWrapper<OrderRefund>()
                .eq(status != null, OrderRefund::getStatus, status)
                .orderByDesc(OrderRefund::getCreateTime)
                .last("LIMIT 200"));
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditRefund(RefundAuditDTO dto) {
        OrderRefund refund = getRefund(dto.getRefundId());
        if (dto.getShopId() != null) {
            requireShopOrder(refund.getOrderId(), dto.getShopId());
        }
        if (refund.getStatus() != null && refund.getStatus() != 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "售后单已审核");
        }
        OrderInfo orderInfo = orderInfoMapper.selectById(refund.getOrderId());
        refund.setAuditTime(LocalDateTime.now());
        refund.setAuditRemark(dto.getAuditRemark());
        if (Boolean.TRUE.equals(dto.getApproved())) {
            refund.setStatus(1);
            try {
                Result<Map<String, Object>> pay = paymentFeignClient.queryByOrderNo(refund.getOrderNo());
                if (pay != null && pay.isSuccess() && pay.getData() != null && pay.getData().get("payNo") != null) {
                    PaymentRefundCommand command = new PaymentRefundCommand();
                    command.setPayNo(pay.getData().get("payNo").toString());
                    command.setRefundAmount(refund.getRefundAmount());
                    command.setRefundReason(refund.getRefundDesc());
                    paymentFeignClient.refund(command);
                }
            } catch (Exception ex) {
                log.warn("支付退款调用失败 refundId={}: {}", refund.getId(), ex.getMessage());
            }
            refund.setStatus(20);
            refund.setRefundTime(LocalDateTime.now());
            if (orderInfo != null) {
                patchOrder(orderInfo, wrapper -> wrapper.set(OrderInfo::getStatus, -30));
            }
        } else {
            refund.setStatus(2);
            if (orderInfo != null) {
                patchOrder(orderInfo, wrapper -> wrapper.set(OrderInfo::getStatus, 20));
            }
        }
        orderRefundMapper.updateById(refund);
    }

    private void fillSkuFromProduct(CreateOrderDTO dto) {
        for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
            try {
                Result<Map<String, Object>> remote = productFeignClient.getSku(item.getSkuId());
                if (remote == null || !remote.isSuccess() || remote.getData() == null) {
                    continue;
                }
                Map<String, Object> sku = remote.getData();
                if (item.getPrice() == null && sku.get("price") != null) {
                    item.setPrice(new BigDecimal(sku.get("price").toString()));
                }
                if (item.getSpuId() == null && sku.get("spuId") != null) {
                    item.setSpuId(Long.parseLong(sku.get("spuId").toString()));
                }
                if (!StringUtils.hasText(item.getSkuName()) && sku.get("skuName") != null) {
                    item.setSkuName(sku.get("skuName").toString());
                }
                if (!StringUtils.hasText(item.getSkuCode()) && sku.get("skuCode") != null) {
                    item.setSkuCode(sku.get("skuCode").toString());
                }
                if (!StringUtils.hasText(item.getSkuImage()) && sku.get("skuImage") != null) {
                    item.setSkuImage(sku.get("skuImage").toString());
                }
                if (item.getShopId() == null && sku.get("shopId") != null) {
                    item.setShopId(Long.parseLong(sku.get("shopId").toString()));
                }
            } catch (Exception ex) {
                log.warn("拉取SKU失败 skuId={}: {}", item.getSkuId(), ex.getMessage());
            }
            if (item.getPrice() == null) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "商品价格不可用，无法下单");
            }
        }
        if (dto.getShopId() == null && dto.getItems() != null) {
            dto.getItems().stream()
                    .map(CreateOrderDTO.OrderItemDTO::getShopId)
                    .filter(id -> id != null)
                    .findFirst()
                    .ifPresent(dto::setShopId);
        }
    }

    public OrderInfo requireShopOrder(Long orderId, Long shopId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        Long owner = orderInfo.getShopId() == null ? 1L : orderInfo.getShopId();
        if (shopId != null && !owner.equals(shopId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能操作其他店铺的订单");
        }
        return orderInfo;
    }

    public OrderDetailVO getMerchantOrderDetail(Long orderId, Long shopId) {
        return toDetailVo(requireShopOrder(orderId, shopId));
    }

    public OrderDetailVO getMerchantOrderDetailByNo(String orderNo, Long shopId) {
        OrderInfo orderInfo = findOrderByNo(orderNo);
        if (orderInfo == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        Long owner = orderInfo.getShopId() == null ? 1L : orderInfo.getShopId();
        if (shopId != null && !owner.equals(shopId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能操作其他店铺的订单");
        }
        return toDetailVo(orderInfo);
    }

    private void fillAddressIfNeeded(CreateOrderDTO dto) {
        if (hasReceiver(dto)) {
            return;
        }
        Map<String, Object> address = fetchAddress(dto.getUserId(), dto.getAddressId());
        if (address == null) {
            address = fetchDefaultAddress(dto.getUserId());
        }
        if (address != null) {
            applyAddress(dto, address);
        }
        if (!hasReceiver(dto)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "请选择有效的收货地址");
        }
    }

    private boolean hasReceiver(CreateOrderDTO dto) {
        return StringUtils.hasText(dto.getReceiverName())
                && StringUtils.hasText(dto.getReceiverPhone())
                && StringUtils.hasText(dto.getReceiverDetailAddress());
    }

    private Map<String, Object> fetchAddress(Long userId, Long addressId) {
        if (userId == null || addressId == null) {
            return null;
        }
        try {
            Result<Map<String, Object>> remote = userFeignClient.getAddress(userId, addressId);
            if (remote != null && remote.isSuccess()) {
                return remote.getData();
            }
        } catch (Exception ex) {
            log.warn("按ID拉取收货地址失败 addressId={}: {}", addressId, ex.getMessage());
        }
        return null;
    }

    private Map<String, Object> fetchDefaultAddress(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            Result<List<Map<String, Object>>> remote = userFeignClient.listAddress(userId);
            if (remote == null || !remote.isSuccess() || remote.getData() == null || remote.getData().isEmpty()) {
                return null;
            }
            return remote.getData().stream()
                    .filter(item -> "1".equals(asString(item.get("isDefault"))))
                    .findFirst()
                    .orElse(remote.getData().get(0));
        } catch (Exception ex) {
            log.warn("拉取用户地址列表失败 userId={}: {}", userId, ex.getMessage());
            return null;
        }
    }

    private void applyAddress(CreateOrderDTO dto, Map<String, Object> address) {
        if (!StringUtils.hasText(dto.getReceiverName())) {
            dto.setReceiverName(firstText(address, "receiverName", "name"));
        }
        if (!StringUtils.hasText(dto.getReceiverPhone())) {
            dto.setReceiverPhone(firstText(address, "receiverPhone", "phone", "mobile"));
        }
        if (!StringUtils.hasText(dto.getReceiverProvince())) {
            dto.setReceiverProvince(firstText(address, "province", "receiverProvince"));
        }
        if (!StringUtils.hasText(dto.getReceiverCity())) {
            dto.setReceiverCity(firstText(address, "city", "receiverCity"));
        }
        if (!StringUtils.hasText(dto.getReceiverDistrict())) {
            dto.setReceiverDistrict(firstText(address, "district", "receiverDistrict"));
        }
        if (!StringUtils.hasText(dto.getReceiverDetailAddress())) {
            dto.setReceiverDetailAddress(firstText(address, "detailAddress", "receiverDetailAddress", "address"));
        }
    }

    private String firstText(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            String value = asString(source.get(key));
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private void tagPromotionRemark(CreateOrderDTO dto) {
        if (StringUtils.hasText(dto.getBuyerRemark())) {
            return;
        }
        if (dto.getSeckillId() != null) {
            dto.setBuyerRemark("秒杀订单");
        } else if (dto.getGroupBuyingId() != null) {
            dto.setBuyerRemark("拼团订单");
        }
    }

    private void applySeckillPrice(CreateOrderDTO dto) {
        if (dto.getSeckillId() == null) {
            return;
        }
        try {
            Result<Map<String, Object>> remote = promotionFeignClient.getSeckill(dto.getSeckillId());
            if (remote == null || !remote.isSuccess() || remote.getData() == null) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "秒杀活动不可用");
            }
            Map<String, Object> activity = remote.getData();
            if (activity.get("seckillPrice") == null || activity.get("skuId") == null) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "秒杀活动信息不完整");
            }
            Long skuId = Long.parseLong(activity.get("skuId").toString());
            BigDecimal seckillPrice = new BigDecimal(activity.get("seckillPrice").toString());
            boolean matched = false;
            for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
                if (skuId.equals(item.getSkuId())) {
                    item.setPrice(seckillPrice);
                    matched = true;
                }
            }
            if (!matched) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单商品与秒杀活动不匹配");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "拉取秒杀价失败");
        }
    }

    private void applyGroupPrice(CreateOrderDTO dto) {
        if (dto.getGroupBuyingId() == null) {
            return;
        }
        try {
            Result<Map<String, Object>> remote = promotionFeignClient.getGroupActivity(dto.getGroupBuyingId());
            if (remote == null || !remote.isSuccess() || remote.getData() == null) {
                return;
            }
            Map<String, Object> activity = remote.getData();
            if (activity.get("groupPrice") == null || activity.get("skuId") == null) {
                return;
            }
            Long skuId = Long.parseLong(activity.get("skuId").toString());
            BigDecimal groupPrice = new BigDecimal(activity.get("groupPrice").toString());
            for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
                if (skuId.equals(item.getSkuId())) {
                    item.setPrice(groupPrice);
                }
            }
        } catch (Exception ex) {
            log.warn("拉取拼团价失败 activityId={}: {}", dto.getGroupBuyingId(), ex.getMessage());
        }
    }

    private BigDecimal calculateFullDiscount(CreateOrderDTO dto, BigDecimal totalAmount) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", totalAmount);
            payload.put("activityId", dto.getFullDiscountId());
            payload.put("spuIds", dto.getItems().stream()
                    .map(CreateOrderDTO.OrderItemDTO::getSpuId)
                    .filter(id -> id != null)
                    .toList());
            Result<Map<String, Object>> remote = promotionFeignClient.calculateFullDiscount(payload);
            if (remote != null && remote.isSuccess() && remote.getData() != null
                    && remote.getData().get("discountAmount") != null) {
                return new BigDecimal(remote.getData().get("discountAmount").toString());
            }
        } catch (Exception ex) {
            log.warn("满减试算失败: {}", ex.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private void bindGroupOrder(CreateOrderDTO dto, Long orderId, String orderNo) {
        if (dto.getGroupRecordId() == null) {
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("groupRecordId", dto.getGroupRecordId());
            payload.put("userId", dto.getUserId());
            payload.put("orderId", orderId);
            payload.put("orderNo", orderNo);
            promotionFeignClient.bindGroupOrder(payload);
        } catch (Exception ex) {
            log.warn("绑定拼团订单失败 orderId={}: {}", orderId, ex.getMessage());
        }
    }

    private void bindSeckillOrder(CreateOrderDTO dto, Long orderId, String orderNo) {
        if (dto.getSeckillRecordId() == null) {
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("recordId", dto.getSeckillRecordId());
            payload.put("userId", dto.getUserId());
            payload.put("orderId", orderId);
            payload.put("orderNo", orderNo);
            promotionFeignClient.bindSeckillOrder(payload);
        } catch (Exception ex) {
            log.warn("绑定秒杀订单失败 orderId={}: {}", orderId, ex.getMessage());
        }
    }

    private BigDecimal quoteFreight(CreateOrderDTO dto, BigDecimal orderAmount, int quantity) {
        try {
            FreightQuoteCommand command = new FreightQuoteCommand();
            command.setTemplateId(dto.getFreightTemplateId() != null ? dto.getFreightTemplateId() : 1L);
            command.setProvince(dto.getReceiverProvince());
            command.setQuantity(quantity);
            command.setOrderAmount(orderAmount);
            Result<BigDecimal> remote = logisticsFeignClient.quoteFreight(command);
            if (remote != null && remote.isSuccess() && remote.getData() != null) {
                return remote.getData();
            }
        } catch (Exception ex) {
            log.warn("运费试算失败，按0处理: {}", ex.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal quoteCoupon(CreateOrderDTO dto, BigDecimal payable) {
        if (dto.getCouponId() == null || payable.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        try {
            Result<BigDecimal> remote = promotionFeignClient.quoteCoupon(dto.getCouponId(), dto.getUserId(), payable);
            if (remote != null && remote.isSuccess() && remote.getData() != null) {
                BigDecimal discount = remote.getData();
                return discount.compareTo(payable) > 0 ? payable : discount;
            }
            if (remote != null && !remote.isSuccess()) {
                throw new BusinessException(ResultCode.COUPON_NOT_AVAILABLE, remote.getMessage());
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.COUPON_NOT_AVAILABLE, "优惠券不可用");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal quotePoints(CreateOrderDTO dto, BigDecimal payable) {
        if (dto.getUsePoints() == null || dto.getUsePoints() <= 0 || payable.compareTo(BigDecimal.ZERO) <= 0) {
            dto.setUsePoints(0);
            return BigDecimal.ZERO;
        }
        int available = 0;
        try {
            Result<Map<String, Object>> remote = accountFeignClient.getPoints(dto.getUserId());
            if (remote != null && remote.isSuccess() && remote.getData() != null
                    && remote.getData().get("availablePoints") != null) {
                available = Integer.parseInt(remote.getData().get("availablePoints").toString());
            }
        } catch (Exception ex) {
            log.warn("查询积分失败 userId={}: {}", dto.getUserId(), ex.getMessage());
        }
        int maxByAmount = payable.multiply(new BigDecimal("100")).intValue();
        int actual = Math.min(dto.getUsePoints(), Math.min(available, maxByAmount));
        if (actual <= 0) {
            dto.setUsePoints(0);
            return BigDecimal.ZERO;
        }
        dto.setUsePoints(actual);
        return new BigDecimal(actual).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
    }

    private void consumeCoupon(CreateOrderDTO dto, OrderCreateVO vo) {
        if (dto.getCouponId() == null) {
            return;
        }
        try {
            Result<Void> remote = promotionFeignClient.useCoupon(dto.getCouponId(), vo.getOrderId(), vo.getOrderNo());
            if (remote == null || !remote.isSuccess()) {
                log.warn("核销优惠券失败 orderId={}: {}", vo.getOrderId(),
                        remote == null ? "empty" : remote.getMessage());
            }
        } catch (Exception ex) {
            log.warn("核销优惠券失败 orderId={}: {}", vo.getOrderId(), ex.getMessage());
        }
    }

    private void consumePoints(CreateOrderDTO dto, OrderCreateVO vo) {
        if (dto.getUsePoints() == null || dto.getUsePoints() <= 0) {
            return;
        }
        try {
            Result<Void> remote = accountFeignClient.deductPoints(dto.getUserId(), dto.getUsePoints(), vo.getOrderId());
            if (remote == null || !remote.isSuccess()) {
                log.warn("扣减积分失败 orderId={}: {}", vo.getOrderId(),
                        remote == null ? "empty" : remote.getMessage());
            }
        } catch (Exception ex) {
            log.warn("扣减积分失败 orderId={}: {}", vo.getOrderId(), ex.getMessage());
        }
    }

    public Map<String, Object> report(LocalDateTime start, LocalDateTime end) {
        return report(start, end, null);
    }

    public Map<String, Object> report(LocalDateTime start, LocalDateTime end, Long shopId) {
        LambdaQueryWrapper<OrderInfo> query = new LambdaQueryWrapper<>();
        query.ge(start != null, OrderInfo::getCreateTime, start);
        query.lt(end != null, OrderInfo::getCreateTime, end);
        query.eq(shopId != null, OrderInfo::getShopId, shopId);
        List<OrderInfo> orders = orderInfoMapper.selectList(query);
        BigDecimal totalPay = BigDecimal.ZERO;
        long unpaid = 0;
        long paid = 0;
        long shipped = 0;
        long cancelled = 0;
        for (OrderInfo order : orders) {
            if (order.getPayAmount() != null && order.getStatus() != null && order.getStatus() >= 10) {
                totalPay = totalPay.add(order.getPayAmount());
            }
            Integer status = order.getStatus();
            if (status == null) {
                continue;
            }
            if (status == 0) {
                unpaid++;
            } else if (status >= 10 && status < 30) {
                paid++;
            } else if (status >= 30) {
                shipped++;
            } else if (status < 0) {
                cancelled++;
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("orderCount", orders.size());
        data.put("payAmount", totalPay);
        data.put("unpaidCount", unpaid);
        data.put("paidCount", paid);
        data.put("shippedCount", shipped);
        data.put("cancelledCount", cancelled);

        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<Map<String, Object>> daily = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate day = today.minusDays(i);
            java.time.LocalDateTime dayStart = day.atStartOfDay();
            java.time.LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();
            long dayCount = 0;
            BigDecimal dayAmount = BigDecimal.ZERO;
            for (OrderInfo order : orders) {
                if (order.getCreateTime() == null) {
                    continue;
                }
                if (order.getCreateTime().isBefore(dayStart) || !order.getCreateTime().isBefore(dayEnd)) {
                    continue;
                }
                dayCount++;
                if (order.getPayAmount() != null && order.getStatus() != null && order.getStatus() >= 10) {
                    dayAmount = dayAmount.add(order.getPayAmount());
                }
            }
            Map<String, Object> row = new HashMap<>();
            row.put("date", day.toString());
            row.put("orderCount", dayCount);
            row.put("payAmount", dayAmount);
            daily.add(row);
        }
        data.put("daily", daily);
        return data;
    }

    public void closeExpiredUnpaidOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(30);
        List<OrderInfo> expired = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, 0)
                .lt(OrderInfo::getCreateTime, deadline)
                .last("LIMIT 50"));
        for (OrderInfo order : expired) {
            try {
                cancelOrder(order.getId());
                log.info("超时关闭未支付订单 orderId={}", order.getId());
            } catch (Exception ex) {
                log.warn("超时关单失败 orderId={}: {}", order.getId(), ex.getMessage());
            }
        }
    }

    private void notifyUser(Long userId, String title, String content, String link) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("type", 1);
            payload.put("title", title);
            payload.put("content", content);
            payload.put("linkUrl", link);
            notificationFeignClient.send(payload);
        } catch (Exception ex) {
            log.warn("发送下单通知失败 userId={}: {}", userId, ex.getMessage());
        }
    }

    private OrderDetailVO toDetailVo(OrderInfo orderInfo) {
        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, orderInfo.getId());
        if (orderInfo.getUserId() != null) {
            itemQuery.eq(OrderItem::getUserId, orderInfo.getUserId());
        }
        List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);
        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(orderInfo, vo);
        vo.setStatusDesc(getOrderStatusDesc(orderInfo.getStatus()));
        String province = orderInfo.getReceiverProvince() == null ? "" : orderInfo.getReceiverProvince();
        String city = orderInfo.getReceiverCity() == null ? "" : orderInfo.getReceiverCity();
        String district = orderInfo.getReceiverDistrict() == null ? "" : orderInfo.getReceiverDistrict();
        String detail = orderInfo.getReceiverDetailAddress() == null ? "" : orderInfo.getReceiverDetailAddress();
        vo.setReceiverAddress(province + city + district + detail);
        vo.setItems(orderItems.stream().map(item -> {
            OrderDetailVO.OrderItemVO itemVO = new OrderDetailVO.OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList()));
        return vo;
    }

    private OrderInfo requireOrder(Long orderId) {
        OrderInfo orderInfo = findOrderById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return orderInfo;
    }

    private OrderInfo findOrderById(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getId, orderId)
                .last("LIMIT 1"));
    }

    private OrderInfo findOrderByNo(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return null;
        }
        return orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo)
                .last("LIMIT 1"));
    }

    /**
     * 只更新业务字段，绝不 SET user_id（分片键）。WHERE 带上 user_id 方便路由。
     */
    private void patchOrder(OrderInfo current, Consumer<LambdaUpdateWrapper<OrderInfo>> setter) {
        LambdaUpdateWrapper<OrderInfo> wrapper = new LambdaUpdateWrapper<OrderInfo>()
                .eq(OrderInfo::getId, current.getId())
                .eq(current.getUserId() != null, OrderInfo::getUserId, current.getUserId())
                .set(OrderInfo::getUpdateTime, LocalDateTime.now());
        setter.accept(wrapper);
        int rows = orderInfoMapper.update(null, wrapper);
        if (rows == 0) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String getOrderStatusDesc(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        return switch (status) {
            case 0 -> "待支付";
            case 10 -> "已支付";
            case 20 -> "待发货";
            case 30 -> "已发货";
            case 40 -> "已收货";
            case 50 -> "已完成";
            case -10 -> "已取消";
            case -20 -> "退款中";
            case -30 -> "已退款";
            default -> "未知状态";
        };
    }

}
