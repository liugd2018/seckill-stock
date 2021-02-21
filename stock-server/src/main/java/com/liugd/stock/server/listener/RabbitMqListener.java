package com.liugd.stock.server.listener;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.liugd.stock.common.constant.Constant;
import com.liugd.stock.common.dto.StockInfoDto;
import com.liugd.stock.common.dto.StockProductDto;
import com.liugd.stock.common.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Slf4j
@Component
public class RabbitMqListener {


    @Resource
    LoadingCache<String,Object> loadingCache;

    /**
     * 删除本地缓存中商品信息
     * @param stockInfoDtoList
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(),
            exchange = @Exchange(value = Constant.RabbitQueue.CLEAN_LOADING_CACHE_EXCHANGE, type = ExchangeTypes.FANOUT)))
    public void cleanLoadingCache(List<StockInfoDto> stockInfoDtoList) {
        log.info("删除本地缓存中商品信息. stockProductDto:{}", JsonUtil.toStringNoException(stockInfoDtoList));

        stockInfoDtoList.stream()
                .filter(Objects::nonNull)
                .forEach(stockInfoDto -> {
                    String loadingCacheKey = stockInfoDto.getStoreCode()
                            .concat(Constant.SEPARATION).concat(stockInfoDto.getProductId());
                    loadingCache.invalidate(loadingCacheKey);
                });

    }
}
