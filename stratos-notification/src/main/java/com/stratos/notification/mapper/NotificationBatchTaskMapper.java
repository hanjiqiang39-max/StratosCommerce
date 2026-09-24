package com.stratos.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.notification.entity.NotificationBatchTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 批量通知任务 Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface NotificationBatchTaskMapper extends BaseMapper<NotificationBatchTask> {
}
