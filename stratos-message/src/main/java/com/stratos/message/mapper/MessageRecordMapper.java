package com.stratos.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.message.entity.MessageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息记录Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface MessageRecordMapper extends BaseMapper<MessageRecord> {
}
