package com.stratos.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.order.dto.CartItemDTO;
import com.stratos.order.entity.ShoppingCart;
import com.stratos.order.mapper.ShoppingCartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public List<ShoppingCart> list(Long userId) {
        return shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .orderByDesc(ShoppingCart::getId));
    }

    public Long add(CartItemDTO dto) {
        ShoppingCart exist = shoppingCartMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, dto.getUserId())
                .eq(ShoppingCart::getSkuId, dto.getSkuId()));
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + dto.getQuantity());
            exist.setSelected(1);
            shoppingCartMapper.updateById(exist);
            return exist.getId();
        }
        ShoppingCart cart = new ShoppingCart();
        cart.setId(snowflakeIdGenerator.nextId());
        cart.setUserId(dto.getUserId());
        cart.setSkuId(dto.getSkuId());
        cart.setSpuId(dto.getSpuId());
        cart.setQuantity(dto.getQuantity());
        cart.setSelected(1);
        shoppingCartMapper.insert(cart);
        return cart.getId();
    }

    public void updateQuantity(Long cartId, Long userId, Integer quantity) {
        ShoppingCart cart = shoppingCartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "购物车不存在");
        }
        if (quantity <= 0) {
            shoppingCartMapper.deleteById(cartId);
            return;
        }
        cart.setQuantity(quantity);
        shoppingCartMapper.updateById(cart);
    }

    public void delete(Long cartId, Long userId) {
        ShoppingCart cart = shoppingCartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "购物车不存在");
        }
        shoppingCartMapper.deleteById(cartId);
    }

    public void clearSelected(Long userId) {
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getSelected, 1));
    }

    public void updateSelected(Long cartId, Long userId, Integer selected) {
        ShoppingCart cart = shoppingCartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "购物车不存在");
        }
        cart.setSelected(selected != null && selected == 1 ? 1 : 0);
        shoppingCartMapper.updateById(cart);
    }

    public void updateSelectedAll(Long userId, Integer selected) {
        int value = selected != null && selected == 1 ? 1 : 0;
        for (ShoppingCart cart : list(userId)) {
            cart.setSelected(value);
            shoppingCartMapper.updateById(cart);
        }
    }

    public List<ShoppingCart> listSelected(Long userId) {
        return shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getSelected, 1));
    }

}
