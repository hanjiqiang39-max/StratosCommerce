package com.stratos.logistics.express;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快递轨迹节点
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class ExpressTrace {

    private LocalDateTime time;

    private String status;

    private String desc;

    private String location;
}
