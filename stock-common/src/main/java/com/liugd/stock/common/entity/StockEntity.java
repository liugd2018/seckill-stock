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
@TableName("t_stock")
public class StockEntity extends BaseEntity {

    @TableId("stock_id")
    private String stockId;

    @TableField("product_name")
    private String productName;

    @TableField("store_code")
    private String storeCode;

    @TableField("product_id")
    private String productId;

    @TableField("total_num")
    private Long totalNum;

    @TableField("lock_num")
    private Long lockNum;

    @TableField("surplus_num")
    private Long surplusNum;

}
