package com.stratos.promotion.controller;

import com.stratos.common.result.Result;
import com.stratos.promotion.dto.BindGroupOrderDTO;
import com.stratos.promotion.dto.GroupJoinDTO;
import com.stratos.promotion.entity.PromotionGroupBuying;
import com.stratos.promotion.entity.PromotionGroupMember;
import com.stratos.promotion.entity.PromotionGroupRecord;
import com.stratos.promotion.service.GroupBuyingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "拼团")
@RestController
@RequestMapping({"/group", "/promotion/group"})
@RequiredArgsConstructor
public class GroupBuyingController {

    private final GroupBuyingService groupBuyingService;

    @Operation(summary = "进行中的拼团活动")
    @GetMapping("/list")
    public Result<List<PromotionGroupBuying>> list() {
        return Result.success(groupBuyingService.listActive());
    }

    @Operation(summary = "开团或参团")
    @PostMapping("/join")
    public Result<PromotionGroupRecord> join(@Valid @RequestBody GroupJoinDTO dto) {
        return Result.success(groupBuyingService.join(dto));
    }

    @Operation(summary = "团详情")
    @GetMapping("/{groupNo}")
    public Result<PromotionGroupRecord> detail(@PathVariable String groupNo) {
        return Result.success(groupBuyingService.getRecord(groupNo));
    }

    @Operation(summary = "团成员")
    @GetMapping("/{groupNo}/members")
    public Result<List<PromotionGroupMember>> members(@PathVariable String groupNo) {
        return Result.success(groupBuyingService.listMembers(groupNo));
    }

    @Operation(summary = "订单是否允许支付（成团后才可付）")
    @GetMapping("/payable")
    public Result<Boolean> payable(@RequestParam("orderId") Long orderId) {
        return Result.success(groupBuyingService.canPay(orderId));
    }

    @Operation(summary = "绑定订单到拼团成员")
    @PostMapping("/bind-order")
    public Result<Void> bindOrder(@Valid @RequestBody BindGroupOrderDTO dto) {
        groupBuyingService.bindOrder(dto);
        return Result.success();
    }

    @GetMapping("/activity/{activityId}")
    public Result<PromotionGroupBuying> activity(@PathVariable Long activityId) {
        return Result.success(groupBuyingService.getActivity(activityId));
    }

    @Operation(summary = "活动下未满员的团")
    @GetMapping("/activity/{activityId}/records")
    public Result<List<PromotionGroupRecord>> openRecords(@PathVariable Long activityId) {
        return Result.success(groupBuyingService.listOpenRecords(activityId));
    }

    @Operation(summary = "我参加的团")
    @GetMapping("/user/{userId}")
    public Result<List<PromotionGroupRecord>> userRecords(@PathVariable Long userId) {
        return Result.success(groupBuyingService.listUserRecords(userId));
    }

    @Operation(summary = "某活动下我参加的团")
    @GetMapping("/user/{userId}/activity/{activityId}")
    public Result<com.stratos.promotion.vo.GroupUserVO> userActivity(@PathVariable Long userId, @PathVariable Long activityId) {
        return Result.success(groupBuyingService.findUserRecordVO(activityId, userId));
    }

    @Operation(summary = "按订单查拼团")
    @GetMapping("/order/{orderId}")
    public Result<PromotionGroupRecord> byOrder(@PathVariable Long orderId) {
        return Result.success(groupBuyingService.findByOrderId(orderId));
    }
}
