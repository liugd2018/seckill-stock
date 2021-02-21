package com.liugd.stock.server.server;

import com.liugd.stock.common.dto.StockInfoDto;
import com.liugd.stock.common.dto.StockProductDto;
import com.liugd.stock.server.exception.BusinessException;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
public interface StockServer {

    /**
     * 锁库存
     * @param stockProductDto
     * @return
     * @throws BusinessException
     */
    void lockStock(StockProductDto stockProductDto) throws BusinessException;


    /**
     * 添加库存
     * @param stockInfoDtoList
     * @return 导入失败库存信息
     * @throws BusinessException
     *
     */
    List<StockInfoDto> addStock(List<StockInfoDto> stockInfoDtoList) throws BusinessException ;

    /**
     * 解锁库存
     * @param stockProductDto
     * @throws BusinessException
     */
    void unlock(StockProductDto stockProductDto) throws BusinessException;


    /**
     * 修改库存
     * @param stockInfoDtoList
     * @throws BusinessException
     */
    void updateStock(List<StockInfoDto> stockInfoDtoList) throws BusinessException ;
}
