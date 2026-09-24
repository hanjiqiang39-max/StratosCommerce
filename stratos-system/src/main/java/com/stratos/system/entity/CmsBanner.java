package com.stratos.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cms_banner")
public class CmsBanner extends BaseEntity {

    @TableId
    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;

    /**
     * 1=首页
     */
    private Integer position;

    private Integer sortOrder;

    /**
     * 0=下线 1=上线
     */
    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
