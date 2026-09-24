package com.stratos.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.message.entity.MessageInbox;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内信Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface MessageInboxMapper extends BaseMapper<MessageInbox> {
}
