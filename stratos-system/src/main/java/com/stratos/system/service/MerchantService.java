package com.stratos.system.service;

import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.system.dto.MerchantAuditDTO;
import com.stratos.system.dto.MerchantRegisterDTO;
import com.stratos.system.dto.MerchantShopDTO;
import com.stratos.system.entity.Merchant;
import com.stratos.system.entity.MerchantShop;
import com.stratos.system.vo.MerchantLoginVO;
import com.stratos.system.vo.MerchantProfileVO;

public interface MerchantService {

    MerchantLoginVO login(String username, String password);

    MerchantLoginVO smsLogin(String phone, String code);

    Long register(MerchantRegisterDTO dto);

    MerchantProfileVO profile(Long merchantId);

    MerchantShop getShop(Long merchantId);

    MerchantShop getShopById(Long shopId);

    Long updateShop(MerchantShopDTO dto);

    PageResult<Merchant> listMerchants(PageQuery pageQuery, Integer auditStatus);

    java.util.List<MerchantShop> listShops();

    void audit(MerchantAuditDTO dto);

    void updateStatus(Long merchantId, Integer status);
}
