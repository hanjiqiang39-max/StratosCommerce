package com.stratos.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.message.entity.MessageNotice;
import com.stratos.message.mapper.MessageNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final MessageNoticeMapper messageNoticeMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public List<MessageNotice> listPublished() {
        LocalDateTime now = LocalDateTime.now();
        return messageNoticeMapper.selectList(new LambdaQueryWrapper<MessageNotice>()
                .eq(MessageNotice::getPublishStatus, 1)
                .and(wrapper -> wrapper.isNull(MessageNotice::getExpireTime).or().ge(MessageNotice::getExpireTime, now))
                .orderByDesc(MessageNotice::getTopFlag)
                .orderByAsc(MessageNotice::getSortOrder)
                .orderByDesc(MessageNotice::getPublishTime));
    }

    public List<MessageNotice> adminList() {
        return messageNoticeMapper.selectList(new LambdaQueryWrapper<MessageNotice>()
                .orderByDesc(MessageNotice::getCreateTime));
    }

    public MessageNotice get(Long id) {
        MessageNotice notice = messageNoticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "公告不存在");
        }
        return notice;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long save(MessageNotice notice) {
        if (!StringUtils.hasText(notice.getTitle()) || !StringUtils.hasText(notice.getContent())) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "标题和内容不能为空");
        }
        if (notice.getId() == null) {
            notice.setId(snowflakeIdGenerator.nextId());
            if (notice.getNoticeType() == null) {
                notice.setNoticeType(1);
            }
            if (notice.getTargetScope() == null) {
                notice.setTargetScope(0);
            }
            if (notice.getPublishStatus() == null) {
                notice.setPublishStatus(0);
            }
            if (notice.getTopFlag() == null) {
                notice.setTopFlag(0);
            }
            if (notice.getSortOrder() == null) {
                notice.setSortOrder(0);
            }
            if (notice.getReadCount() == null) {
                notice.setReadCount(0);
            }
            if (notice.getPublishStatus() == 1 && notice.getPublishTime() == null) {
                notice.setPublishTime(LocalDateTime.now());
            }
            messageNoticeMapper.insert(notice);
        } else {
            MessageNotice db = get(notice.getId());
            if (notice.getPublishStatus() != null && notice.getPublishStatus() == 1 && notice.getPublishTime() == null) {
                notice.setPublishTime(db.getPublishTime() == null ? LocalDateTime.now() : db.getPublishTime());
            }
            notice.setVersion(db.getVersion());
            messageNoticeMapper.updateById(notice);
        }
        return notice.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id, Integer status) {
        MessageNotice notice = get(id);
        int publishStatus = status == null ? 1 : status;
        LambdaUpdateWrapper<MessageNotice> update = new LambdaUpdateWrapper<MessageNotice>()
                .eq(MessageNotice::getId, id)
                .set(MessageNotice::getPublishStatus, publishStatus);
        if (publishStatus == 1 && notice.getPublishTime() == null) {
            update.set(MessageNotice::getPublishTime, LocalDateTime.now());
        }
        messageNoticeMapper.update(null, update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        messageNoticeMapper.deleteById(id);
    }
}
