package com.stratos.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "product_comment", excludeProperty = {"createBy", "updateBy"})
public class ProductComment extends BaseEntity {

    @TableId
    private Long id;

    private Long spuId;

    private Long skuId;

    private Long userId;

    private Long orderId;

    private String nickname;

    private Integer starRating;

    private String content;

    private String images;

    private String specDesc;

    private String replyContent;

    private LocalDateTime replyTime;

    private Integer likesCount;

    private Integer isAnonymous;

    private Integer auditStatus;

    private Integer showStatus;
}
