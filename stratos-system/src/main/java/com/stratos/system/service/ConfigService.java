package com.stratos.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.system.entity.SysConfig;

/**
 * 系统配置服务接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface ConfigService extends IService<SysConfig> {

    /**
     * 根据配置键获取配置�?
     */
    String getConfigValueByKey(String configKey);

    java.util.List<SysConfig> listAll();

    boolean saveConfig(SysConfig config);

    boolean deleteConfig(Long id);

}
