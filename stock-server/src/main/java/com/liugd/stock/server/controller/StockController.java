package com.liugd.stock.server.controller;

import com.liugd.stock.common.dto.StockInfoDto;
import com.liugd.stock.common.dto.StockProductDto;
import com.liugd.stock.server.exception.BusinessException;
import com.liugd.stock.server.server.StockServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/stock")
public class StockController {

    @Resource
    StockServer stockServer;

    /**
     * 扣减库存
     * @param stockProductDto
     * @throws BusinessException
     */
    @PostMapping("/lock")

    public void stockLock(@RequestBody StockProductDto stockProductDto) throws BusinessException {
        // TODO 幂等需要添加
        try {
            stockServer.lockStock(stockProductDto);
        }catch (BusinessException e){
            throw e;
        } catch (Exception e){
            log.error("未知异常", e);
            throw new BusinessException(500,"未知异常");
        }

    }

    /**
     * 添加库存
     * @param stockInfoDtoList
     * @return
     * @throws BusinessException
     */
    @PostMapping("/add")
    public List<StockInfoDto> addStock(@RequestBody List<StockInfoDto> stockInfoDtoList) throws BusinessException {
        try {
            return stockServer.addStock(stockInfoDtoList);
        }catch (BusinessException e){
            throw e;
        } catch (Exception e){
            log.error("未知异常", e);
            throw new BusinessException(500,"未知异常");
        }
    }

    /**
     * 修改库存
     * @param stockInfoDtoList
     * @throws BusinessException
     */
    @PostMapping("/update")
    public void updateStock(@RequestBody List<StockInfoDto> stockInfoDtoList) throws BusinessException {
        try {
            stockServer.updateStock(stockInfoDtoList);
        }catch (BusinessException e){
            throw e;
        } catch (Exception e){
            log.error("未知异常", e);
            throw new BusinessException(500,"未知异常");
        }
    }
}
