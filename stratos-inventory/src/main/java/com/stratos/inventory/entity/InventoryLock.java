package com.stratos.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 库存锁定记录。表无 create_by / update_by / is_deleted。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "inventory_lock", excludeProperty = {"createBy", "updateBy", "isDeleted"})
public class InventoryLock extends BaseEntity {

    private Long id;

    private Long skuId;

    private Long warehouseId;

    private Integer lockQuantity;

    /**
     * 锁定类型：1=秒杀，2=订单
     */
    private Integer lockType;

    private Long sourceId;

    private String sourceNo;

    private Long userId;

    /**
     * 状态：0=锁定中，1=已扣减，2=已释放
     */
    private Integer status;

    private LocalDateTime expireTime;

    private LocalDateTime deductTime;

    private LocalDateTime releaseTime;

}
