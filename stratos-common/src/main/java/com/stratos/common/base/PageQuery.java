package com.stratos.common.base;

import com.stratos.common.constant.CommonConstants;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求参数基类
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum = CommonConstants.DEFAULT_PAGE_NUM;

    /**
     * 每页大小
     */
    private Integer pageSize = CommonConstants.DEFAULT_PAGE_SIZE;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 是否升序
     */
    private Boolean isAsc = true;

    public Integer getPageNum() {
        if (pageNum == null || pageNum < 1) {
            return CommonConstants.DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return CommonConstants.DEFAULT_PAGE_SIZE;
        }
        if (pageSize > CommonConstants.MAX_PAGE_SIZE) {
            return CommonConstants.MAX_PAGE_SIZE;
        }
        return pageSize;
    }

}
