package com.liugd.stock.common.enums;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */

public enum OrderStatusEnum {
    INIT("1","正常下单"),
    PAY("2","已支付"),
    OVER("3","下单结束");

    private String code;
    private String name;
    OrderStatusEnum(String code,String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public static OrderStatusEnum match(String code){
        for (OrderStatusEnum item: OrderStatusEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

}
