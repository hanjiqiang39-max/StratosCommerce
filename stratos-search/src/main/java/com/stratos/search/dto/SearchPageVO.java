package com.stratos.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索分页结果。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchPageVO<T> {

    private List<T> records;

    private long total;

    private int page;

    private int size;

}
