package com.stratos.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.system.entity.CmsBanner;
import com.stratos.system.mapper.CmsBannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final CmsBannerMapper cmsBannerMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public List<CmsBanner> listPublished(Integer position) {
        LocalDateTime now = LocalDateTime.now();
        return cmsBannerMapper.selectList(new LambdaQueryWrapper<CmsBanner>()
                .eq(CmsBanner::getStatus, 1)
                .eq(position != null, CmsBanner::getPosition, position)
                .and(wrapper -> wrapper.isNull(CmsBanner::getStartTime).or().le(CmsBanner::getStartTime, now))
                .and(wrapper -> wrapper.isNull(CmsBanner::getEndTime).or().ge(CmsBanner::getEndTime, now))
                .orderByAsc(CmsBanner::getSortOrder)
                .orderByDesc(CmsBanner::getCreateTime));
    }

    public List<CmsBanner> adminList() {
        return cmsBannerMapper.selectList(new LambdaQueryWrapper<CmsBanner>()
                .orderByAsc(CmsBanner::getSortOrder)
                .orderByDesc(CmsBanner::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long save(CmsBanner banner) {
        if (banner.getId() == null) {
            banner.setId(snowflakeIdGenerator.nextId());
            if (banner.getPosition() == null) {
                banner.setPosition(1);
            }
            if (banner.getStatus() == null) {
                banner.setStatus(1);
            }
            if (banner.getSortOrder() == null) {
                banner.setSortOrder(0);
            }
            cmsBannerMapper.insert(banner);
        } else {
            cmsBannerMapper.updateById(banner);
        }
        return banner.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        cmsBannerMapper.deleteById(id);
    }
}
