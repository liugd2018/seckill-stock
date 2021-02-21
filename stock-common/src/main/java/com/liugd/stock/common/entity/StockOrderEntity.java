package com.liugd.stock.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.liugd.stock.common.base.BaseEntity;
import lombok.Data;


/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Data
@TableName("t_order_stock")
public class StockOrderEntity extends BaseEntity {

    @TableId("stock_order_id")
    private String stockOrderId;

    @TableField("stock_id")
    private String stockId;

    @TableField("order_id")
    private String orderId;

    @TableField("store_code")
    private String storeCode;

    @TableField("product_id")
    private String productId;

    @TableField("lock_num")
    private Long lockNum;

    @TableField("lock_time")
    private Long lockTime;

}
