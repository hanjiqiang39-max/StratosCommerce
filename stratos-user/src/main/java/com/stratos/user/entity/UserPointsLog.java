package com.stratos.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_points_log")
public class UserPointsLog {

    @TableId
    private Long id;

    private Long userId;

    private Integer changeType;

    private Integer changePoints;

    private Integer beforePoints;

    private Integer afterPoints;

    private Integer sourceType;

    private Long sourceId;

    private LocalDateTime expireTime;

    private String remark;

    private LocalDateTime createTime;
}
