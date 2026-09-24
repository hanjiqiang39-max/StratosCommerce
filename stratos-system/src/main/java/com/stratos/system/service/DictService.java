package com.stratos.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.system.entity.SysDictData;
import com.stratos.system.entity.SysDictType;

import java.util.List;

/**
 * 字典服务接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface DictService extends IService<SysDictType> {

    /**
     * 根据字典类型编码查询字典数据
     */
    List<SysDictData> getDictDataByType(String dictType);

    List<SysDictType> listTypes();

    boolean saveType(SysDictType type);

    boolean deleteType(Long id);

    boolean saveData(SysDictData data);

    boolean deleteData(Long id);

}
