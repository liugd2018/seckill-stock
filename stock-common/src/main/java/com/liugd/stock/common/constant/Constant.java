package com.liugd.stock.common.constant;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
public class Constant {

    /** 重试次数 */
    public static final int RETRY_COUNT = 10;

    /** 锁定设置超时间时间 */
    public static final long LOCK_TIMEOUT = 500;

    public static final String SELL_OUT = "商品已经售罄.";

    public static final String NON_EXISTENT = "商品不存在";


    public static final String SEPARATION = "_";

    /**
     * 最后一次锁时间redis字段名字
     */
    public static final String LAST_LOCK_TIME = "lastLockTime";

    public static final String ORDER_STATUS = "orderStatus";




    /**
     * redis 商品库存结构
     */
    public interface RedisProductStock{

        /**
         * 剩余数量
         */
        String SURPLUS_NUM = "surplusNum";

        /**
         * 总库存数
         */
        String TOTAL_NUM = "totalNum";

        /**
         * 商品名称
         */
        String PRODUCT_NAME = "productName";
    }

    /**
     *
     * redis商品前缀
     */
    public interface RedisPrefix{

        /**
         * 锁redis查询mysql库存同步redis
         */
        String PRODUCT_QUERY_LOCK_PREFIX = "product_query:lock_";

        /**
         * 修改订单前缀
         */
        String ORDER_LOCK_PREFIX = "lock:lock_order_";

        /**
         * 添加库存锁商品前缀
         */
        String PRODUCT_LOCK_ADD = "lock:lock_add_";

        /**
         * MYSQL商品同步redis前缀
         */
        String PRODUCT_SYNC_PREFIX  = "product:sync_";

        /**
         * 商品redis前缀
         */
        String PRODUCT_PREFIX = "product:";

        /**
         * 订单redis前缀
         */
        String ORDER_PREFIX = "order:";
    }

    /**
     * mq队列
     */
    public interface RabbitQueue{

        /**
         * 锁定商品队列 (同步mysql)
         */
        String LOCK_QUEUE_NAME = "product.lock.queue";

        /**
         * 锁定商品exchange (同步mysql)
         */
        String LOCK_EXCHANGE_NAME = "product.lock.exchange";

        /**
         * 锁定商品routing key (同步mysql)
         */
        String LOCK_ROUTING_KEY = "product.lock.routing.key";


        /**
         * 锁定商品队列 (同步mysql)
         */
        String UNLOCK_QUEUE_NAME = "product.unlock.queue";

        /**
         * 锁定商品exchange (同步mysql)
         */
        String UNLOCK_EXCHANGE_NAME = "product.unlock.exchange";

        /**
         * 锁定商品routing key (同步mysql)
         */
        String UNLOCK_ROUTING_KEY = "product.unlock.routing.key";

        /**
         * 自动解锁商品队列
         */
        String AUTO_UNLOCK_QUEUE_NAME = "product.auto.unlock.queue";

        /**
         * 自动解锁商品exchange
         */
        String AUTO_UNLOCK_EXCHANGE_NAME = "product.auto.unlock.exchange";

        /**
         * 自动解锁商品routing key
         */
        String AUTO_UNLOCK_ROUTING_KEY = "product.auto.unlock.routing.key";

        /**
         * 自动解锁延时商品队列
         */
        String AUTO_UNLOCK_DELAY_QUEUE_NAME = "product.auto.unlock.delay.queue";

        /**
         * 自动解锁延时商品exchange
         */
        String AUTO_UNLOCK_DELAY_EXCHANGE_NAME = "product.auto.unlock.delay.exchange";

        /**
         * 自动解锁延时商品routing key
         */
        String AUTO_UNLOCK_DELAY_ROUTING_KEY = "product.auto.unlock.delay.routing.key";

        /**
         * 添加库存同步mysql (队列)
         */
        String SYNC_ADD_PRODUCT_QUEUE_NAME = "product.sync.add.product.queue";


        /**
         * 添加库存同步mysql （exchange）
         */
        String SYNC_ADD_PRODUCT_EXCHANGE =  "product.sync.add.product.exchange";


        /**
         * 添加库存同步mysql (routing key)
         */
        String SYNC_ADD_PRODUCT_ROUTING_KEY = "product.sync.add.product.routing.key";

        /**
         * 添加库存同步mysql (队列)
         */
        String SYNC_UPDATE_PRODUCT_QUEUE_NAME = "product.sync.update.product.queue";


        /**
         * 添加库存同步mysql （exchange）
         */
        String SYNC_UPDATE_PRODUCT_EXCHANGE =  "product.sync.update.product.exchange";


        /**
         * 添加库存同步mysql (routing key)
         */
        String SYNC_UPDATE_PRODUCT_ROUTING_KEY = "product.sync.update.product.routing.key";



        /**
         * 清除本地缓存 exchange
         */
        String CLEAN_LOADING_CACHE_EXCHANGE = "clean.loadingCache.exchange";
    }
}
