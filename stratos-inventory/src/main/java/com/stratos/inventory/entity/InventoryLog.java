package com.stratos.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存变更日志。表仅有 create_time，无审计/软删字段。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "inventory_log", excludeProperty = {"updateTime", "createBy", "updateBy", "isDeleted"})
public class InventoryLog extends BaseEntity {

    private Long id;

    private Long skuId;

    private Long warehouseId;

    /**
     * 变更类型：1=入库，2=出库，3=锁定，4=解锁，5=退货，6=盘点
     */
    private Integer changeType;

    private Integer changeQuantity;

    private Integer beforeStock;

    private Integer afterStock;

    /**
     * 来源类型：1=采购入库，2=订单出库，3=退货入库，4=秒杀锁定
     */
    private Integer sourceType;

    private Long sourceId;

    private String sourceNo;

    private Long operatorId;

    private String operatorName;

    private String remark;

}
