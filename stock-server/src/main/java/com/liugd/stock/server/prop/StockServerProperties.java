package com.liugd.stock.server.prop;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Data
@Component
public class StockServerProperties {


    @Value("${stock.auto-unlock.ttl}")
    private long autoUnlockTtl;

    @Value("${stock.product.ttl}")
    private long productTtl;

    @Value("${stock.lock.ttl}")
    private long lockTtl;

    @Value("${stock.redisson.lock}")
    private long redissonLockTtl ;


    @Value("${stock.order.storage-time}")
    private long orderStorageTime;

    public long getAutoUnlockTtl() {
        return autoUnlockTtl;
    }

    public long getProductTtl() {
        // 加上随机数,防止雪崩
        return productTtl * new Random().nextInt(10);
    }

    public long getLockTtl() {
        return lockTtl;
    }

    public long getRedissonLockTtl() {
        return redissonLockTtl;
    }

    public long getOrderStorageTime() {
        return orderStorageTime;
    }
}
