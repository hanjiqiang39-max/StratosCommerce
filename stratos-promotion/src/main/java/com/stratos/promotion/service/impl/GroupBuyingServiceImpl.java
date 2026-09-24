package com.stratos.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.promotion.dto.BindGroupOrderDTO;
import com.stratos.promotion.dto.GroupJoinDTO;
import com.stratos.promotion.entity.PromotionGroupBuying;
import com.stratos.promotion.entity.PromotionGroupMember;
import com.stratos.promotion.entity.PromotionGroupRecord;
import com.stratos.promotion.mapper.PromotionGroupBuyingMapper;
import com.stratos.promotion.mapper.PromotionGroupMemberMapper;
import com.stratos.promotion.mapper.PromotionGroupRecordMapper;
import com.stratos.promotion.service.GroupBuyingService;
import com.stratos.promotion.vo.GroupUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupBuyingServiceImpl implements GroupBuyingService {

    private final PromotionGroupBuyingMapper groupBuyingMapper;
    private final PromotionGroupRecordMapper recordMapper;
    private final PromotionGroupMemberMapper memberMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public List<PromotionGroupBuying> listActive() {
        expireTimeouts();
        LocalDateTime now = LocalDateTime.now();
        return groupBuyingMapper.selectList(new LambdaQueryWrapper<PromotionGroupBuying>()
                .eq(PromotionGroupBuying::getStatus, 1)
                .le(PromotionGroupBuying::getStartTime, now)
                .ge(PromotionGroupBuying::getEndTime, now));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromotionGroupRecord join(GroupJoinDTO dto) {
        expireTimeouts();
        PromotionGroupBuying activity = groupBuyingMapper.selectById(dto.getActivityId());
        if (activity == null || activity.getStatus() == null || activity.getStatus() != 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "拼团活动不可用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "拼团活动不在有效期");
        }
        if (StringUtils.hasText(dto.getGroupNo())) {
            return joinExisting(activity, dto, now);
        }
        return openNew(activity, dto, now);
    }

    @Override
    public PromotionGroupRecord getRecord(String groupNo) {
        expireTimeouts();
        return recordMapper.selectOne(new LambdaQueryWrapper<PromotionGroupRecord>()
                .eq(PromotionGroupRecord::getGroupNo, groupNo));
    }

    @Override
    public List<PromotionGroupMember> listMembers(String groupNo) {
        return memberMapper.selectList(new LambdaQueryWrapper<PromotionGroupMember>()
                .eq(PromotionGroupMember::getGroupNo, groupNo)
                .orderByAsc(PromotionGroupMember::getJoinTime));
    }

    @Override
    public boolean canPay(Long orderId) {
        expireTimeouts();
        PromotionGroupMember member = memberMapper.selectOne(new LambdaQueryWrapper<PromotionGroupMember>()
                .eq(PromotionGroupMember::getOrderId, orderId)
                .last("LIMIT 1"));
        if (member == null) {
            return true;
        }
        PromotionGroupRecord record = recordMapper.selectById(member.getGroupRecordId());
        return record != null && record.getStatus() != null && record.getStatus() == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindOrder(BindGroupOrderDTO dto) {
        memberMapper.update(null, new LambdaUpdateWrapper<PromotionGroupMember>()
                .eq(PromotionGroupMember::getGroupRecordId, dto.getGroupRecordId())
                .eq(PromotionGroupMember::getUserId, dto.getUserId())
                .set(PromotionGroupMember::getOrderId, dto.getOrderId())
                .set(PromotionGroupMember::getOrderNo, dto.getOrderNo()));
    }

    @Override
    public PromotionGroupBuying getActivity(Long activityId) {
        return groupBuyingMapper.selectById(activityId);
    }

    @Override
    public List<PromotionGroupRecord> listOpenRecords(Long activityId) {
        expireTimeouts();
        return recordMapper.selectList(new LambdaQueryWrapper<PromotionGroupRecord>()
                .eq(PromotionGroupRecord::getGroupBuyingId, activityId)
                .eq(PromotionGroupRecord::getStatus, 0)
                .gt(PromotionGroupRecord::getExpireTime, LocalDateTime.now())
                .orderByDesc(PromotionGroupRecord::getCreateTime));
    }

    @Override
    public List<PromotionGroupRecord> listUserRecords(Long userId) {
        expireTimeouts();
        List<PromotionGroupMember> members = memberMapper.selectList(new LambdaQueryWrapper<PromotionGroupMember>()
                .eq(PromotionGroupMember::getUserId, userId)
                .orderByDesc(PromotionGroupMember::getJoinTime));
        if (members.isEmpty()) {
            return List.of();
        }
        List<Long> ids = members.stream().map(PromotionGroupMember::getGroupRecordId).distinct().toList();
        return recordMapper.selectList(new LambdaQueryWrapper<PromotionGroupRecord>()
                .in(PromotionGroupRecord::getId, ids)
                .orderByDesc(PromotionGroupRecord::getCreateTime));
    }

    @Override
    public PromotionGroupRecord findUserRecord(Long activityId, Long userId) {
        expireTimeouts();
        List<PromotionGroupMember> members = memberMapper.selectList(new LambdaQueryWrapper<PromotionGroupMember>()
                .eq(PromotionGroupMember::getUserId, userId)
                .orderByDesc(PromotionGroupMember::getJoinTime));
        for (PromotionGroupMember member : members) {
            PromotionGroupRecord record = recordMapper.selectById(member.getGroupRecordId());
            if (record != null && activityId.equals(record.getGroupBuyingId())) {
                return record;
            }
        }
        return null;
    }

    @Override
    public PromotionGroupRecord findByOrderId(Long orderId) {
        expireTimeouts();
        PromotionGroupMember member = memberMapper.selectOne(new LambdaQueryWrapper<PromotionGroupMember>()
                .eq(PromotionGroupMember::getOrderId, orderId)
                .last("LIMIT 1"));
        if (member == null) {
            return null;
        }
        return recordMapper.selectById(member.getGroupRecordId());
    }

    @Override
    public GroupUserVO findUserRecordVO(Long activityId, Long userId) {
        PromotionGroupRecord record = findUserRecord(activityId, userId);
        if (record == null) {
            return null;
        }
        PromotionGroupMember member = memberMapper.selectOne(new LambdaQueryWrapper<PromotionGroupMember>()
                .eq(PromotionGroupMember::getGroupRecordId, record.getId())
                .eq(PromotionGroupMember::getUserId, userId)
                .last("LIMIT 1"));
        GroupUserVO vo = new GroupUserVO();
        vo.setId(record.getId());
        vo.setGroupBuyingId(record.getGroupBuyingId());
        vo.setGroupNo(record.getGroupNo());
        vo.setLeaderUserId(record.getLeaderUserId());
        vo.setRequireNum(record.getRequireNum());
        vo.setCurrentNum(record.getCurrentNum());
        vo.setStatus(record.getStatus());
        vo.setExpireTime(record.getExpireTime());
        vo.setSuccessTime(record.getSuccessTime());
        if (member != null) {
            vo.setOrderId(member.getOrderId() != null && member.getOrderId() > 0 ? member.getOrderId() : null);
            vo.setOrderNo(member.getOrderNo());
            vo.setIsLeader(member.getIsLeader());
        }
        return vo;
    }

    private PromotionGroupRecord openNew(PromotionGroupBuying activity, GroupJoinDTO dto, LocalDateTime now) {
        long joined = memberMapper.selectCount(new LambdaQueryWrapper<PromotionGroupMember>()
                .eq(PromotionGroupMember::getUserId, dto.getUserId())
                .inSql(PromotionGroupMember::getGroupRecordId,
                        "SELECT id FROM promotion_group_record WHERE group_buying_id = "
                                + activity.getId() + " AND status IN (0,1)"));
        if (activity.getLimitPerUser() != null && joined >= activity.getLimitPerUser()) {
            PromotionGroupRecord existing = findUserRecord(activity.getId(), dto.getUserId());
            if (existing != null) {
                return existing;
            }
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "超过每人参团次数");
        }
        PromotionGroupRecord record = new PromotionGroupRecord();
        record.setId(snowflakeIdGenerator.nextId());
        record.setGroupBuyingId(activity.getId());
        record.setGroupNo("GB" + System.currentTimeMillis());
        record.setLeaderUserId(dto.getUserId());
        record.setRequireNum(activity.getRequireNum());
        record.setCurrentNum(1);
        record.setStatus(activity.getRequireNum() != null && activity.getRequireNum() <= 1 ? 1 : 0);
        int hours = activity.getLimitHours() == null ? 24 : activity.getLimitHours();
        record.setExpireTime(now.plusHours(hours));
        if (record.getStatus() == 1) {
            record.setSuccessTime(now);
        }
        record.setCreateTime(now);
        record.setUpdateTime(now);
        recordMapper.insert(record);
        insertMember(record, dto, 1, now);
        return record;
    }

    private PromotionGroupRecord joinExisting(PromotionGroupBuying activity, GroupJoinDTO dto, LocalDateTime now) {
        PromotionGroupRecord record = getRecord(dto.getGroupNo());
        if (record == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团不存在");
        }
        if (record.getStatus() == null || record.getStatus() != 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该团已结束");
        }
        if (record.getExpireTime() != null && now.isAfter(record.getExpireTime())) {
            failGroup(record, now);
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该团已超时失败");
        }
        Long exists = memberMapper.selectCount(new LambdaQueryWrapper<PromotionGroupMember>()
                .eq(PromotionGroupMember::getGroupRecordId, record.getId())
                .eq(PromotionGroupMember::getUserId, dto.getUserId()));
        if (exists != null && exists > 0) {
            return record;
        }
        insertMember(record, dto, 0, now);
        int next = (record.getCurrentNum() == null ? 0 : record.getCurrentNum()) + 1;
        record.setCurrentNum(next);
        record.setUpdateTime(now);
        if (next >= record.getRequireNum()) {
            record.setStatus(1);
            record.setSuccessTime(now);
        }
        recordMapper.updateById(record);
        return record;
    }

    private void insertMember(PromotionGroupRecord record, GroupJoinDTO dto, int leader, LocalDateTime now) {
        PromotionGroupMember member = new PromotionGroupMember();
        member.setId(snowflakeIdGenerator.nextId());
        member.setGroupRecordId(record.getId());
        member.setGroupNo(record.getGroupNo());
        member.setUserId(dto.getUserId());
        member.setOrderId(dto.getOrderId() == null ? 0L : dto.getOrderId());
        member.setOrderNo(StringUtils.hasText(dto.getOrderNo()) ? dto.getOrderNo() : "");
        member.setIsLeader(leader);
        member.setJoinTime(now);
        memberMapper.insert(member);
    }

    private void expireTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        List<PromotionGroupRecord> expired = recordMapper.selectList(new LambdaQueryWrapper<PromotionGroupRecord>()
                .eq(PromotionGroupRecord::getStatus, 0)
                .lt(PromotionGroupRecord::getExpireTime, now));
        for (PromotionGroupRecord record : expired) {
            failGroup(record, now);
        }
    }

    private void failGroup(PromotionGroupRecord record, LocalDateTime now) {
        record.setStatus(2);
        record.setUpdateTime(now);
        recordMapper.updateById(record);
    }
}
