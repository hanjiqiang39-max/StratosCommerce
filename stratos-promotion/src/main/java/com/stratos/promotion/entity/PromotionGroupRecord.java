package com.stratos.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拼团记录
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@TableName("promotion_group_record")
public class PromotionGroupRecord {

    private Long id;

    private Long groupBuyingId;

    private String groupNo;

    private Long leaderUserId;

    private Integer requireNum;

    private Integer currentNum;

    /**
     * 0=拼团中，1=拼团成功，2=拼团失败
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime successTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
