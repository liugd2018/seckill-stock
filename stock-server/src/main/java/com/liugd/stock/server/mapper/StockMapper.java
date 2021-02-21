package com.liugd.stock.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liugd.stock.common.entity.StockEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Mapper
public interface StockMapper extends BaseMapper<StockEntity> {


    /**
     * 批量添加库存信息
     *
     * @param stockEntityList
     * @return
     */
    int insertStockList(List<StockEntity> stockEntityList);


    /**
     * 插入库存数据
     * @param stockEntity
     * @return
     */
    int insertStockExistUpdateStock(StockEntity stockEntity);

    /**
     * 锁商品
     * @param storeCode
     * @param productId
     * @param num
     * @return
     */
    int updateStockLockProduct(@Param("storeCode") String storeCode,
                               @Param("productId") String productId,
                               @Param("num") long num);


    /**
     * 更新库存
     * @param storeCode
     * @param productId
     * @param addNum
     * @return
     */
    int updateStockProduct(@Param("storeCode") String storeCode,
                           @Param("productId") String productId,
                           @Param("addNum") long addNum);

    /**
     * 解锁商品
     * @param storeCode
     * @param productId
     * @param num
     * @return
     */
    int updateStockUnLockProduct(@Param("storeCode") String storeCode,
                                 @Param("productId") String productId,
                                 @Param("num") long num);


    /**
     * 查询商品信息
     * @param storeCode
     * @param productId
     * @return
     */
    StockEntity queryStockCodeProduct(@Param("storeCode") String storeCode,
                                      @Param("productId") String productId);
}
