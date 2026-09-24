package com.stratos.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.user.entity.UserFavorite;
import com.stratos.user.mapper.UserFavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final UserFavoriteMapper userFavoriteMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public Long add(Long userId, Integer targetType, Long targetId) {
        if (userId == null || targetType == null || targetId == null) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "收藏参数不完整");
        }
        UserFavorite exists = userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .eq(UserFavorite::getTargetId, targetId));
        if (exists != null) {
            return exists.getId();
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setId(snowflakeIdGenerator.nextId());
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        favorite.setCreateTime(LocalDateTime.now());
        userFavoriteMapper.insert(favorite);
        return favorite.getId();
    }

    public void remove(Long userId, Integer targetType, Long targetId) {
        userFavoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .eq(UserFavorite::getTargetId, targetId));
    }

    public List<UserFavorite> list(Long userId, Integer targetType) {
        return userFavoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(targetType != null, UserFavorite::getTargetType, targetType)
                .orderByDesc(UserFavorite::getCreateTime));
    }

    public boolean exists(Long userId, Integer targetType, Long targetId) {
        return userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .eq(UserFavorite::getTargetId, targetId)) > 0;
    }
}
