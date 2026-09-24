package com.stratos.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.user.dto.UserAddressDTO;
import com.stratos.user.entity.UserAddress;
import com.stratos.user.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressMapper userAddressMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public List<UserAddress> listByUserId(Long userId) {
        return userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getUpdateTime));
    }

    public UserAddress getById(Long addressId, Long userId) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "地址不存在");
        }
        return address;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, UserAddressDTO dto) {
        if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            clearDefault(userId);
        }
        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(dto, address);
        address.setId(snowflakeIdGenerator.nextId());
        address.setUserId(userId);
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        if (address.getAddressType() == null) {
            address.setAddressType(0);
        }
        userAddressMapper.insert(address);
        return address.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long addressId, Long userId, UserAddressDTO dto) {
        UserAddress address = getById(addressId, userId);
        if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            clearDefault(userId);
        }
        BeanUtils.copyProperties(dto, address);
        address.setId(addressId);
        address.setUserId(userId);
        userAddressMapper.updateById(address);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long addressId, Long userId) {
        getById(addressId, userId);
        userAddressMapper.deleteById(addressId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long addressId, Long userId) {
        getById(addressId, userId);
        clearDefault(userId);
        UserAddress address = new UserAddress();
        address.setId(addressId);
        address.setIsDefault(1);
        userAddressMapper.updateById(address);
    }

    private void clearDefault(Long userId) {
        userAddressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0));
    }

}
