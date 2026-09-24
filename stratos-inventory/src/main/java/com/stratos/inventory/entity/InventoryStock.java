package com.stratos.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存主表。表无 create_by / update_by / is_deleted。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "inventory_stock", excludeProperty = {"createBy", "updateBy", "isDeleted"})
public class InventoryStock extends BaseEntity {

    private Long id;

    private Long skuId;

    private Long warehouseId;

    private Integer totalStock;

    private Integer availableStock;

    /** 锁定库存，对应列 lock_stock */
    private Integer lockStock;

    private Integer soldStock;

    private Integer inTransitStock;

    private Integer lowStockThreshold;

    private Integer status;

    @Version
    private Integer version;

}
