package com.stratos.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拼团成员
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@TableName("promotion_group_member")
public class PromotionGroupMember {

    private Long id;

    private Long groupRecordId;

    private String groupNo;

    private Long userId;

    private Long orderId;

    private String orderNo;

    private Integer isLeader;

    private LocalDateTime joinTime;
}
