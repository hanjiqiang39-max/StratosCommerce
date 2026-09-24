package com.stratos.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 积分账户。表无软删字段。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@TableName("user_points_account")
public class UserPointsAccount {

    private Long id;

    private Long userId;

    private Integer totalPoints;

    private Integer availablePoints;

    private Integer usedPoints;

    private Integer frozenPoints;

    private Integer expirePoints;

    @Version
    private Integer version;

}
