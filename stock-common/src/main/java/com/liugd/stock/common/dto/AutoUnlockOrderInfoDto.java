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
public class AutoUnlockOrderInfoDto implements Serializable {

    /**
     * 订单ID
     */
    private String orderId;

    private long lockTime;

}
