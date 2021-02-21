package com.liugd.stock.listener.listener;

import com.liugd.stock.common.constant.Constant;
import com.liugd.stock.common.dto.AutoUnlockOrderInfoDto;
import com.liugd.stock.common.dto.StockProductSyncMysqlDto;
import com.liugd.stock.common.entity.StockOrderEntity;
import com.liugd.stock.common.utils.JsonUtil;
import com.liugd.stock.listener.mapper.StockOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Slf4j
@Component
public class StockOperationListener {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Resource
    StockOrderMapper stockOrderMapper;

    /**
     * 锁库存order同步数据库
     * @param stockProductDto
     */
    @Transactional(rollbackFor = Throwable.class)
    @RabbitListener(queues = Constant.RabbitQueue.LOCK_QUEUE_NAME)
    public void lockOnMessage(StockProductSyncMysqlDto stockProductDto){

        try {

            log.info("rabbitmq lock接受到消息:{}", JsonUtil.toStringNoException(stockProductDto));

            StockOrderEntity stockOrderEntity = stockOrderMapper.queryStockCodeAndProductIdAndOrderId(stockProductDto.getStoreCode(),
                    stockProductDto.getProductId(), stockProductDto.getOrderId());

            if (Objects.isNull(stockOrderEntity)){
                StockOrderEntity stockOrderEntityInsert = new StockOrderEntity();
                stockOrderEntityInsert.setLockNum(1L);
                stockOrderEntityInsert.setProductId(stockProductDto.getProductId());
                stockOrderEntityInsert.setStoreCode(stockProductDto.getStoreCode());
                stockOrderEntityInsert.setOrderId(stockProductDto.getOrderId());
                stockOrderEntityInsert.setStockOrderId(UUID.randomUUID().toString());
                stockOrderEntityInsert.setLockTime(stockProductDto.getLockTime());
                stockOrderEntityInsert.setCreateUser("stock");
                stockOrderEntityInsert.setUpdateUser("stock");
                int count = stockOrderMapper.insertStockExistUpdateStock(stockOrderEntityInsert);
            } else {
                int count = stockOrderMapper.updateStockLock(stockProductDto.getStoreCode(),
                        stockProductDto.getProductId(),stockProductDto.getOrderId(), stockProductDto.getNum(),"stock");
            }


        }catch (Exception e){
            log.error("json转换失败.", e);
            // TODO 进入死信队列
        }

    }

    /**
     * 自动解锁延时队列
     */
    @RabbitListener(queues = Constant.RabbitQueue.AUTO_UNLOCK_QUEUE_NAME)
    public void autoUnlockOnMessage(AutoUnlockOrderInfoDto autoUnlockOrderInfoDto){

        log.info("rabbitmq 自动解锁. message:{}", JsonUtil.toStringNoException(autoUnlockOrderInfoDto));
        Map<Object, Object> orderInfoMap = redisTemplate.opsForHash().entries(autoUnlockOrderInfoDto.getOrderId());

        if (orderInfoMap.isEmpty()){
            // mysql查询订单数据
        } else {

            Object object = orderInfoMap.get(Constant.LAST_LOCK_TIME);

            if (autoUnlockOrderInfoDto.getLockTime() != Long.parseLong(String.valueOf(object))){
                log.info("rabbitmq 自动解锁消息丢弃 >>> message:{}",JsonUtil.toStringNoException(autoUnlockOrderInfoDto));
                return;
            }

            // TODO 判断这个订单是否支付或者被下单

            // TODO 调用解锁商品接口



        }





    }


}
