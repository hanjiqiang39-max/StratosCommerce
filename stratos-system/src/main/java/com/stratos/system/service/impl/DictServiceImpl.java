package com.stratos.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.system.entity.SysDictData;
import com.stratos.system.entity.SysDictType;
import com.stratos.system.mapper.SysDictDataMapper;
import com.stratos.system.mapper.SysDictTypeMapper;
import com.stratos.system.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 字典服务实现
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements DictService {

    private final SysDictDataMapper dictDataMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DICT_KEY = "dict:type:";
    private static final long EXPIRE_HOURS = 24;

    @Override
    public List<SysDictData> getDictDataByType(String dictType) {
        // 先从Redis获取
        String key = DICT_KEY + dictType;
        List<SysDictData> cached = (List<SysDictData>) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        // 数据库查�?
        List<SysDictData> dataList = dictDataMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysDictData>()
                        .eq("dict_type", dictType)
                        .eq("status", 1)
                        .orderByAsc("dict_sort")
        );

        // 缓存到Redis
        redisTemplate.opsForValue().set(key, dataList, EXPIRE_HOURS, TimeUnit.HOURS);
        return dataList;
    }

    @Override
    public List<SysDictType> listTypes() {
        return list();
    }

    @Override
    public boolean saveType(SysDictType type) {
        boolean ok = saveOrUpdate(type);
        evict(type.getDictType());
        return ok;
    }

    @Override
    public boolean deleteType(Long id) {
        SysDictType type = getById(id);
        boolean ok = removeById(id);
        if (type != null) {
            evict(type.getDictType());
        }
        return ok;
    }

    @Override
    public boolean saveData(SysDictData data) {
        boolean ok;
        if (data.getId() != null && dictDataMapper.selectById(data.getId()) != null) {
            ok = dictDataMapper.updateById(data) > 0;
        } else {
            ok = dictDataMapper.insert(data) > 0;
        }
        evict(data.getDictType());
        return ok;
    }

    @Override
    public boolean deleteData(Long id) {
        SysDictData data = dictDataMapper.selectById(id);
        boolean ok = dictDataMapper.deleteById(id) > 0;
        if (data != null) {
            evict(data.getDictType());
        }
        return ok;
    }

    private void evict(String dictType) {
        if (dictType != null) {
            redisTemplate.delete(DICT_KEY + dictType);
        }
    }

}
