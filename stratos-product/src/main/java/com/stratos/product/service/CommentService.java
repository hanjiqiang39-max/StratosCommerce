package com.stratos.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.product.dto.SaveCommentDTO;
import com.stratos.product.entity.ProductComment;
import com.stratos.product.entity.ProductSpu;
import com.stratos.product.mapper.ProductCommentMapper;
import com.stratos.product.mapper.ProductSpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final ProductCommentMapper productCommentMapper;
    private final ProductSpuMapper productSpuMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Transactional(rollbackFor = Exception.class)
    public Long add(SaveCommentDTO dto) {
        Long exists = productCommentMapper.selectCount(new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getUserId, dto.getUserId())
                .eq(ProductComment::getOrderId, dto.getOrderId())
                .eq(ProductComment::getSkuId, dto.getSkuId()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "该商品已评价");
        }
        ProductComment comment = new ProductComment();
        comment.setId(snowflakeIdGenerator.nextId());
        comment.setSpuId(dto.getSpuId());
        comment.setSkuId(dto.getSkuId());
        comment.setUserId(dto.getUserId());
        comment.setOrderId(dto.getOrderId());
        boolean anonymous = dto.getIsAnonymous() != null && dto.getIsAnonymous() == 1;
        comment.setNickname(anonymous ? "匿名用户" : dto.getNickname());
        comment.setStarRating(dto.getStarRating());
        comment.setContent(dto.getContent());
        comment.setImages(dto.getImages());
        comment.setSpecDesc(dto.getSpecDesc());
        comment.setIsAnonymous(dto.getIsAnonymous() == null ? 0 : dto.getIsAnonymous());
        comment.setAuditStatus(1);
        comment.setShowStatus(1);
        comment.setLikesCount(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        productCommentMapper.insert(comment);
        return comment.getId();
    }

    public List<ProductComment> listBySpu(Long spuId) {
        return productCommentMapper.selectList(new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getSpuId, spuId)
                .eq(ProductComment::getShowStatus, 1)
                .eq(ProductComment::getAuditStatus, 1)
                .orderByDesc(ProductComment::getCreateTime));
    }

    public List<ProductComment> adminList(Integer auditStatus) {
        return productCommentMapper.selectList(new LambdaQueryWrapper<ProductComment>()
                .eq(auditStatus != null, ProductComment::getAuditStatus, auditStatus)
                .orderByDesc(ProductComment::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public void reply(Long id, String replyContent) {
        ProductComment comment = productCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "评价不存在");
        }
        comment.setReplyContent(replyContent);
        comment.setReplyTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        productCommentMapper.updateById(comment);
    }

    public List<ProductComment> listByShop(Long shopId) {
        List<ProductSpu> spus = productSpuMapper.selectList(new LambdaQueryWrapper<ProductSpu>()
                .eq(ProductSpu::getShopId, shopId));
        if (spus.isEmpty()) {
            return List.of();
        }
        List<Long> spuIds = spus.stream().map(ProductSpu::getId).toList();
        return productCommentMapper.selectList(new LambdaQueryWrapper<ProductComment>()
                .in(ProductComment::getSpuId, spuIds)
                .orderByDesc(ProductComment::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public void replyByShop(Long id, Long shopId, String replyContent) {
        ProductComment comment = productCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "评价不存在");
        }
        ProductSpu spu = productSpuMapper.selectById(comment.getSpuId());
        Long owner = spu == null || spu.getShopId() == null ? 1L : spu.getShopId();
        if (shopId != null && !owner.equals(shopId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能回复其他店铺的评价");
        }
        reply(id, replyContent);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, Integer auditStatus) {
        ProductComment comment = productCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "评价不存在");
        }
        comment.setAuditStatus(auditStatus);
        comment.setShowStatus(auditStatus != null && auditStatus == 1 ? 1 : 0);
        comment.setUpdateTime(LocalDateTime.now());
        productCommentMapper.updateById(comment);
    }
}
