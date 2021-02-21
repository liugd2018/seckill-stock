package com.liugd.stock.server.server.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.liugd.stock.common.constant.Constant;
import com.liugd.stock.common.dto.AutoUnlockOrderInfoDto;
import com.liugd.stock.common.dto.StockInfoDto;
import com.liugd.stock.common.dto.StockProductDto;
import com.liugd.stock.common.dto.StockProductSyncMysqlDto;
import com.liugd.stock.common.entity.StockEntity;
import com.liugd.stock.common.enums.OrderStatusEnum;
import com.liugd.stock.common.utils.JsonUtil;
import com.liugd.stock.server.convert.StockConvertMapper;
import com.liugd.stock.server.convert.StockProductConvertMapper;
import com.liugd.stock.server.exception.BusinessException;
import com.liugd.stock.server.mapper.StockMapper;
import com.liugd.stock.server.prop.StockServerProperties;
import com.liugd.stock.server.server.StockServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Slf4j
@Service
public class StockServerImpl implements StockServer {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Resource
    LoadingCache<String,Object> loadingCache;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Resource
    StockConvertMapper stockConvertMapper;

    @Resource
    StockProductConvertMapper stockProductConvertMapper;

    @Resource
    StockMapper stockMapper;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    StockServerProperties stockServerProperties;


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void lockStock(StockProductDto stockProductDto) throws BusinessException {

        if (StringUtils.isBlank(stockProductDto.getStoreCode())){
            log.warn("storeCode 不能为空.");
            throw new BusinessException(1000,"storeCode 不能为空.");
        }

        if (StringUtils.isBlank(stockProductDto.getOrderId())){
            log.warn("orderId 不能为空.");
            throw new BusinessException(1000,"orderId 不能为空.");
        }

        if (StringUtils.isBlank(stockProductDto.getProductId())){
            log.warn("productId 不能为空.");
            throw new BusinessException(1000,"productId 不能为空.");
        }

        // 本地缓存的key
        String loadingCacheKey = stockProductDto.getStoreCode()
                .concat(Constant.SEPARATION).concat(stockProductDto.getProductId());

        // check本地缓存是否存在
        Object present  = loadingCache.getIfPresent(loadingCacheKey);
        if (Objects.nonNull(present)){
            if (Constant.NON_EXISTENT.equals(present)){
                log.warn("商品不存在. productId:{}", loadingCacheKey);
                throw new BusinessException(1,"商品不存在.");
            }
            if (Constant.SELL_OUT.equals(present)){
                log.warn("商品已经售罄. productId:{}", loadingCacheKey);
                throw new BusinessException(1,"商品已经售罄.");
            }
        }
        // redis key
        String redisProductKey = Constant.RedisPrefix.PRODUCT_PREFIX
                .concat(stockProductDto.getStoreCode())
                .concat(Constant.SEPARATION)
                .concat(stockProductDto.getProductId());
        // check product 是否存在数据库 同步mysql数据到redis
        checkSyncProduct(redisProductKey,loadingCacheKey, stockProductDto);

        // redis 扣减库存
        long num = redisTemplate.opsForHash().increment(redisProductKey,
                Constant.RedisProductStock.SURPLUS_NUM , -stockProductDto.getNum());
        if (num < 0){
            loadingCache.put(loadingCacheKey, Constant.SELL_OUT);
            redisTemplate.opsForHash().increment(redisProductKey,
                    Constant.RedisProductStock.SURPLUS_NUM , stockProductDto.getNum());
            log.warn("商品已经售罄. productId:{} num:{}", stockProductDto.getProductId(), num);
            throw new BusinessException(1,"商品已经售罄.");
        }

        // mysql 扣减库存
        int count = stockMapper.updateStockLockProduct(stockProductDto.getStoreCode(),
                stockProductDto.getProductId(), stockProductDto.getNum());
        if (count == 0){
            loadingCache.put(loadingCacheKey, Constant.SELL_OUT);
            log.warn("商品没有扣减成功. storeCode:{} productId:{}", stockProductDto.getStoreCode(),
                    stockProductDto.getProductId());
            //  redis未售罄、mysql已售罄 需要同步一下mysql数据
            syncRedisProduct(redisProductKey, stockProductDto.getStoreCode(), stockProductDto.getProductId());
            throw new BusinessException(1,"商品已经售罄.");
        }

        // 设置订单信息
         long lockTime = setOrderInfo(stockProductDto);

        StockProductSyncMysqlDto stockProductSyncMysqlDto = stockProductConvertMapper.toEntity(stockProductDto);
        stockProductSyncMysqlDto.setLockTime(lockTime);

        // 通过MQ异步同步mysql数据库
        rabbitTemplate.convertAndSend(Constant.RabbitQueue.LOCK_EXCHANGE_NAME,Constant.RabbitQueue.LOCK_ROUTING_KEY,stockProductSyncMysqlDto);

        // 设置自动解锁
        AutoUnlockOrderInfoDto autoUnlockOrderInfoDto = new AutoUnlockOrderInfoDto()
                .setOrderId(stockProductDto.getOrderId()).setLockTime(lockTime);
        // 通过延时MQ 自动解锁库存
        // 通过消息设置延时ttl
        rabbitTemplate.convertAndSend(Constant.RabbitQueue.LOCK_EXCHANGE_NAME,Constant.RabbitQueue.LOCK_ROUTING_KEY,autoUnlockOrderInfoDto, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            // 设置这条消息的过期时间 单位：毫秒
            messageProperties.setExpiration(String.valueOf(stockServerProperties.getAutoUnlockTtl()));
            return message;
        });

        // TODO 通过MQ异步记录操作日志表
    }

    /**
     * 同步mysql数据到redis
     * @param redisProductKey
     * @param storeCode
     * @param productId
     */
    Object syncRedisProduct(String redisProductKey, String storeCode, String productId){

        Object object = redisTemplate.opsForHash().get(redisProductKey,Constant.RedisProductStock.SURPLUS_NUM);

        if (!"0".equals(String.valueOf(object))){
            StockEntity stockEntity = stockMapper.queryStockCodeProduct(storeCode, productId);
            if (Objects.isNull(stockEntity)){
                return null;
            }
            // mysql 数据同步Redis
            syncProductMysqlToRedis(stockEntity);
        }

        return object;

    }

    /**
     * check商品数据,并且从mysql同步到redis
     * @param stockProductDto
     * @throws BusinessException
     */
    private void checkSyncProduct(String redisProductKey,
                                  String loadingCacheKey,
                                  StockProductDto stockProductDto) throws BusinessException {
        String lockQueryProduct = Constant.RedisPrefix.PRODUCT_QUERY_LOCK_PREFIX.concat(stockProductDto.getStoreCode())
                .concat(Constant.SEPARATION).concat(stockProductDto.getProductId());


        // check是否存在 不存在场景从mysql同步到redis
        if (!redisTemplate.opsForHash().hasKey(redisProductKey, Constant.RedisProductStock.SURPLUS_NUM)) {
            // 分布式锁 防止缓存失效后,高并发打入数据库
            RLock rLock = redissonClient.getLock(lockQueryProduct);
            try {
                boolean res = rLock.tryLock(stockServerProperties.getRedissonLockTtl(), TimeUnit.MILLISECONDS);
                if (res) {
                    if (!redisTemplate.opsForHash().hasKey(redisProductKey, Constant.RedisProductStock.SURPLUS_NUM)) {
                        StockEntity stockEntity = stockMapper.queryStockCodeProduct(stockProductDto.getStoreCode(), stockProductDto.getProductId());

                        if (Objects.isNull(stockEntity)) {
                            loadingCache.put(loadingCacheKey, Constant.NON_EXISTENT);
                            log.warn("商品不存在. productId:{}", stockProductDto.getProductId());
                            throw new BusinessException(1, "商品不存在.");
                        } else {
                            // mysql 数据同步Redis
                            syncProductMysqlToRedis(stockEntity);
                        }
                    }
                } else {
                    log.warn("product未获取到锁. storeCode_product:{}", redisProductKey);
                    throw new BusinessException(1, "网络繁忙,请重新再试.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(1, "网络繁忙,请重新再试.");
            } finally {
                rLock.unlock();
            }
        }
    }

    /**
     * 同步mysql数据 -> redis
     * @param stockEntity
     */
    private void syncProductMysqlToRedis(StockEntity stockEntity){
        String key = Constant.RedisPrefix.PRODUCT_PREFIX.concat(stockEntity.getStoreCode())
                .concat(Constant.SEPARATION).concat(stockEntity.getProductId());
        Map<String,Object> stockInfoMap = new HashMap<>(3);
        stockInfoMap.put(Constant.RedisProductStock.PRODUCT_NAME,stockEntity.getProductName());
        stockInfoMap.put(Constant.RedisProductStock.TOTAL_NUM,String.valueOf(stockEntity.getTotalNum()));
        stockInfoMap.put(Constant.RedisProductStock.SURPLUS_NUM, String.valueOf(stockEntity.getSurplusNum()));
        redisTemplate.opsForHash().putAll(key,stockInfoMap);
        redisTemplate.expire(key,stockServerProperties.getProductTtl(),TimeUnit.MICROSECONDS);
    }

    /**
     * 设置订单信息
     * @param stockProductDto
     */
    private long setOrderInfo(StockProductDto stockProductDto) throws BusinessException {
        // 设置order 结构
        Map<Object, Object> orderInfoMap = redisTemplate.opsForHash().entries(stockProductDto.getOrderId());
        long lockNum = 0L;
        if (!orderInfoMap.isEmpty()){

            // 存在 商品ID
            if (orderInfoMap.containsKey(stockProductDto.getProductId())){
                lockNum = redisTemplate.opsForHash().increment(stockProductDto.getOrderId(),stockProductDto.getProductId(),stockProductDto.getNum());
            } else {
                // check 是不是存在存在锁
                if (redisTemplate.opsForHash().putIfAbsent(stockProductDto.getOrderId(),stockProductDto.getProductId(),stockProductDto.getNum())){
                    log.info("设置order内商品信息. orderId:{} productId:{} num:{}",stockProductDto.getOrderId(),stockProductDto.getProductId(), stockProductDto.getNum());
                }else {
                    // 停顿一下,在执行设置order内商品信息
                    intervalRetrySetOrder(stockProductDto);
                }
            }
        } else {

            // 不存在order信息,设置order信息
            String lockOrder = Constant.RedisPrefix.ORDER_LOCK_PREFIX.concat(stockProductDto.getOrderId());

            RLock lock = redissonClient.getLock(lockOrder);
            try {
                if (lock.tryLock(stockServerProperties.getRedissonLockTtl(), TimeUnit.MILLISECONDS)){
                    Map<String,Object> stockInfoMap = new HashMap<>(20);
                    stockInfoMap.put(stockProductDto.getProductId(),stockProductDto.getNum()+"");
                    stockInfoMap.put(Constant.ORDER_STATUS, OrderStatusEnum.INIT.getCode());
                    redisTemplate.opsForHash().putAll(stockProductDto.getOrderId(),stockInfoMap);
                    redisTemplate.expire(stockProductDto.getOrderId(),stockServerProperties.getOrderStorageTime(),TimeUnit.MILLISECONDS);
                } else {
                    // 停顿一下,在执行设置order信息
                    intervalRetrySetOrder(stockProductDto);
                }
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
                intervalRetrySetOrder(stockProductDto);
            } finally {
                lock.unlock();
            }

        }
        long lockTime = System.currentTimeMillis();
        // 设置最后一次锁定时间
        redisTemplate.opsForHash().put(stockProductDto.getOrderId(), Constant.LAST_LOCK_TIME, lockTime);

        log.info("商品锁成功.productId:{} lockNum:{}", stockProductDto.getProductId(), lockNum);

        return lockTime;
    }

    /**
     * 设置间隔时间
     * @param stockProductDto
     * @throws BusinessException
     */
    private void intervalRetrySetOrder(StockProductDto stockProductDto) throws BusinessException {
        try {
            Thread.sleep(Constant.LOCK_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        setOrderInfo(stockProductDto);
    }

    /**
     * 解锁库存
     * @param stockProductDto
     * @throws BusinessException
     */
    @Override
    public void unlock(StockProductDto stockProductDto) throws BusinessException {

    }

    /**
     * 更新库存
     * @param stockProductDtoList
     * @throws BusinessException
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void updateStock(List<StockInfoDto> stockProductDtoList) throws BusinessException {
        for (StockInfoDto stockProductDto : stockProductDtoList) {
            String redisProductKey = Constant.RedisPrefix.PRODUCT_PREFIX
                    .concat(stockProductDto.getStoreCode())
                    .concat(Constant.SEPARATION)
                    .concat(stockProductDto.getProductId());
            // check不存在数据 mysql -> redis 同步数据
            if (Objects.isNull(syncRedisProduct(redisProductKey, stockProductDto.getStoreCode(), stockProductDto.getProductId()))){
                log.warn("该商品不存在. param:{}", JsonUtil.toStringNoException(stockProductDto));
                throw new BusinessException(1, "商品不存在");
            }
        }

        for (StockInfoDto stockProductDto : stockProductDtoList) {
            String redisProductKey = Constant.RedisPrefix.PRODUCT_PREFIX
                    .concat(stockProductDto.getStoreCode())
                    .concat(Constant.SEPARATION)
                    .concat(stockProductDto.getProductId());
            Object object = redisTemplate.opsForHash()
                    .get(redisProductKey, Constant.RedisProductStock.SURPLUS_NUM);
            long surplusNum = Long.parseLong((String) object) ;
            long surplusNumSub = 0L;
            // 减去剩余全部库存数量
            if (surplusNum > 0){
                surplusNumSub = redisTemplate.opsForHash()
                        .increment(redisProductKey,Constant.RedisProductStock.SURPLUS_NUM, -surplusNum);
            }

            // 剩余全部库存数量 + 本次添加库存数量
            long stockNumAdd = stockProductDto.getNum() + surplusNum;
            long surplusNumUpdate = redisTemplate.opsForHash()
                    .increment(redisProductKey, Constant.RedisProductStock.SURPLUS_NUM, stockNumAdd);
            // 更新数据库
            stockMapper.updateStockProduct(stockProductDto.getStoreCode(),
                    stockProductDto.getProductId(), stockProductDto.getNum());
            log.info("商品{}库存添加后.surplusNum:{} surplusNumSub:{} stockNumAdd:{} surplusNumUpdate:{}",
                    stockProductDto.getProductId(), surplusNum, surplusNumSub, stockNumAdd, surplusNumUpdate);
        }

        // 通过mq广播通知的方式清除本地缓存数据
        rabbitTemplate.convertAndSend(Constant.RabbitQueue.CLEAN_LOADING_CACHE_EXCHANGE,"",stockProductDtoList);

//        // TODO 通过MQ异步记录操作日志表
//        rabbitTemplate.convertAndSend(Constant.RabbitQueue.SYNC_UPDATE_PRODUCT_EXCHANGE,
//                Constant.RabbitQueue.SYNC_UPDATE_PRODUCT_ROUTING_KEY, stockProductDtoList);
    }


    /**
     * 添加库存
     * @param stockProductDtoList
     * @throws BusinessException
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public List<StockInfoDto> addStock(List<StockInfoDto> stockProductDtoList) throws BusinessException {
        // TODO 幂等添加

        // 存入数据库
        List<StockEntity> stockEntityList = stockProductDtoList.stream()
                .filter(Objects::nonNull)
                .map(stockConvertMapper::toEntity)
                .collect(Collectors.toList());
        int count = stockMapper.insertStockList(stockEntityList);

        log.info("数据已经存入数据库. count:{}", count);

        stockProductDtoList = stockProductDtoList.stream()
                .peek(stockInfoDto -> stockInfoDto.setSurplusNum(stockInfoDto.getNum()))
                .collect(Collectors.toList());
        // 设置redis 中结构
        List<StockInfoDto> importFailStockInfoList = setRedisProductInfo(stockProductDtoList);


        // 通过mq广播通知的方式清除本地缓存数据

        rabbitTemplate.convertAndSend(Constant.RabbitQueue.CLEAN_LOADING_CACHE_EXCHANGE,"",stockProductDtoList);

        // 通过MQ异步同步mysql数据库
        rabbitTemplate.convertAndSend(Constant.RabbitQueue.SYNC_ADD_PRODUCT_EXCHANGE,
                Constant.RabbitQueue.SYNC_ADD_PRODUCT_ROUTING_KEY, stockProductDtoList);

        // TODO 通过MQ异步记录操作日志表

        return importFailStockInfoList;
    }

    /**
     * 设置商品在redis中结构
     * @param stockProductDtoList
     */
    private List<StockInfoDto> setRedisProductInfo(List<StockInfoDto> stockProductDtoList) {

        List<StockInfoDto> importFailStockInfoList = new ArrayList<>();
        /**
         * 商品redis结构: hash
         *  key: product:storeCode_productId 店铺code_商品ID
         *  value:
         *      totalNum: 商品总数量
         *      surplusNum: 剩余商品数量
         *
         */
        for (StockInfoDto stockInfoDto : stockProductDtoList) {

            // 分布式锁key
            String lockProductId = Constant.RedisPrefix.PRODUCT_LOCK_ADD.concat(stockInfoDto.getProductId());
            RLock lock = redissonClient.getLock(lockProductId);
            try {
                String key = Constant.RedisPrefix.PRODUCT_PREFIX.concat(stockInfoDto.getStoreCode())
                        .concat(Constant.SEPARATION).concat(stockInfoDto.getProductId());
                if (lock.tryLock(stockServerProperties.getRedissonLockTtl(), TimeUnit.MILLISECONDS)){
                    Map<String,Object> stockInfoMap = new HashMap<>(5);
                    stockInfoMap.put(Constant.RedisProductStock.PRODUCT_NAME,stockInfoDto.getProductName());
                    stockInfoMap.put(Constant.RedisProductStock.TOTAL_NUM,String.valueOf(stockInfoDto.getNum()));
                    stockInfoMap.put(Constant.RedisProductStock.SURPLUS_NUM, String.valueOf(stockInfoDto.getSurplusNum()));
                    redisTemplate.opsForHash().putAll(key,stockInfoMap);
                    redisTemplate.expire(key,stockServerProperties.getProductTtl(),TimeUnit.MICROSECONDS);
                }else {
                    importFailStockInfoList.add(stockInfoDto);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }

        }

        return importFailStockInfoList;
    }


}
