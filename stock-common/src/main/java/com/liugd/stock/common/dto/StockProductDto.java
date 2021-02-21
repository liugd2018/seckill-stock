package com.liugd.stock.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */

@Data
@Accessors(chain = true)
public class StockProductDto implements Serializable {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 店铺code
     */
    private String storeCode;

    /**
     * 商品name
     */
    private String productName;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品数量
     */
    private long num;
}
