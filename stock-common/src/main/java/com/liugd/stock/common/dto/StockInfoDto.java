package com.liugd.stock.common.dto;

import lombok.Data;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Data
public class StockInfoDto {

    private String storeCode;

    private String productId;

    private String productName;

    private long num;

    private long lockNum;

    private long surplusNum;
}


