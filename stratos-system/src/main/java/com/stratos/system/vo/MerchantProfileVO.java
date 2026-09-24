package com.stratos.system.vo;

import com.stratos.system.entity.Merchant;
import com.stratos.system.entity.MerchantShop;
import lombok.Data;

@Data
public class MerchantProfileVO {

    private Merchant merchant;

    private MerchantShop shop;
}
