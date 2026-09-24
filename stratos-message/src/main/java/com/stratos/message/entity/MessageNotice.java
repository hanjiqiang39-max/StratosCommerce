package com.stratos.message.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_notice")
public class MessageNotice extends BaseEntity {

    @TableId
    private Long id;

    private String title;

    private String content;

    private Integer noticeType;

    private Integer targetScope;

    private String targetValue;

    private Integer publishStatus;

    private LocalDateTime publishTime;

    private LocalDateTime expireTime;

    private Integer topFlag;

    private Integer sortOrder;

    private Integer readCount;

    @Version
    private Integer version;
}
