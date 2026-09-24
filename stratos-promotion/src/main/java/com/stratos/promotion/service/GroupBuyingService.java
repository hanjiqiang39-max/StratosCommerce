package com.stratos.promotion.service;

import com.stratos.promotion.dto.BindGroupOrderDTO;
import com.stratos.promotion.dto.GroupJoinDTO;
import com.stratos.promotion.entity.PromotionGroupBuying;
import com.stratos.promotion.entity.PromotionGroupMember;
import com.stratos.promotion.entity.PromotionGroupRecord;

import java.util.List;

public interface GroupBuyingService {

    List<PromotionGroupBuying> listActive();

    PromotionGroupRecord join(GroupJoinDTO dto);

    PromotionGroupRecord getRecord(String groupNo);

    List<PromotionGroupMember> listMembers(String groupNo);

    boolean canPay(Long orderId);

    void bindOrder(BindGroupOrderDTO dto);

    PromotionGroupBuying getActivity(Long activityId);

    List<PromotionGroupRecord> listOpenRecords(Long activityId);

    List<PromotionGroupRecord> listUserRecords(Long userId);

    PromotionGroupRecord findUserRecord(Long activityId, Long userId);

    PromotionGroupRecord findByOrderId(Long orderId);

    com.stratos.promotion.vo.GroupUserVO findUserRecordVO(Long activityId, Long userId);
}
