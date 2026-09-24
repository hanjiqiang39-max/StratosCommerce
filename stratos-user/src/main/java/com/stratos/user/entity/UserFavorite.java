package com.stratos.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_favorite")
public class UserFavorite {

    @TableId
    private Long id;

    private Long userId;

    /**
     * 1=商品 2=店铺
     */
    private Integer targetType;

    private Long targetId;

    private LocalDateTime createTime;
}
