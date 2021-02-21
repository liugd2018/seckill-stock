package com.liugd.stock.listener.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liugd.stock.common.entity.StockOrderEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
public interface StockOrderMapper extends BaseMapper<StockOrderEntity> {


    /**
     * 批量添加库存信息
     *
     * @param stockOrderEntities
     * @return
     */
    int insertStockList(List<StockOrderEntity> stockOrderEntities);


    /**
     * 插入库存数据
     * @param stockOrderEntity
     * @return
     */
    int insertStockExistUpdateStock(StockOrderEntity stockOrderEntity);


    /**
     * 更新锁库存
     * @param storeCode
     * @param productId
     * @param orderId
     * @param lockNum
     * @param updateUser
     * @return
     */
    int updateStockLock(@Param("storeCode") String storeCode,
                        @Param("productId") String productId,
                        @Param("orderId") String orderId,
                        @Param("lockNum") long lockNum,
                        @Param("updateUser") String updateUser);

    /**
     * 解锁库存
     * @param storeCode
     * @param productId
     * @param orderId
     * @param lockNum
     * @param updateUser
     * @return
     */
    int updateStockUnlock(@Param("storeCode") String storeCode,
                          @Param("productId") String productId,
                          @Param("orderId") String orderId,
                          @Param("lockNum") long lockNum,
                          @Param("updateUser") String updateUser);


    /**
     * 查询订单信息
     * @param storeCode
     * @param productId
     * @param orderId
     * @return
     */
    StockOrderEntity queryStockCodeAndProductIdAndOrderId(@Param("storeCode") String storeCode,
                                      @Param("productId") String productId,
                                      @Param("orderId") String orderId);
}
