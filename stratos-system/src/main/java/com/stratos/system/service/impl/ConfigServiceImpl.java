package com.stratos.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.system.entity.SysConfig;
import com.stratos.system.mapper.SysConfigMapper;
import com.stratos.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务实现
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ConfigService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CONFIG_KEY = "config:";
    private static final long EXPIRE_HOURS = 12;

    @Override
    public String getConfigValueByKey(String configKey) {
        String key = CONFIG_KEY + configKey;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached.toString();
        }

        SysConfig config = lambdaQuery().eq(SysConfig::getConfigKey, configKey).one();
        if (config != null) {
            redisTemplate.opsForValue().set(key, config.getConfigValue(), EXPIRE_HOURS, TimeUnit.HOURS);
            return config.getConfigValue();
        }
        return null;
    }

    @Override
    public java.util.List<SysConfig> listAll() {
        return list();
    }

    @Override
    public boolean saveConfig(SysConfig config) {
        boolean ok = saveOrUpdate(config);
        if (config.getConfigKey() != null) {
            redisTemplate.delete(CONFIG_KEY + config.getConfigKey());
        }
        return ok;
    }

    @Override
    public boolean deleteConfig(Long id) {
        SysConfig config = getById(id);
        boolean ok = removeById(id);
        if (config != null && config.getConfigKey() != null) {
            redisTemplate.delete(CONFIG_KEY + config.getConfigKey());
        }
        return ok;
    }

}
