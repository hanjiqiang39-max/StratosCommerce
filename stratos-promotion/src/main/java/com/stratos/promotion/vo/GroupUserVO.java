package com.stratos.promotion.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupUserVO {

    private Long id;

    private Long groupBuyingId;

    private String groupNo;

    private Long leaderUserId;

    private Integer requireNum;

    private Integer currentNum;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime successTime;

    private Long orderId;

    private String orderNo;

    private Integer isLeader;
}
