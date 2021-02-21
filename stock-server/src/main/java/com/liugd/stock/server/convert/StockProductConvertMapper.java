package com.liugd.stock.server.convert;

import com.liugd.stock.common.base.BaseMapper;
import com.liugd.stock.common.dto.StockProductDto;
import com.liugd.stock.common.dto.StockProductSyncMysqlDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockProductConvertMapper extends BaseMapper<StockProductDto, StockProductSyncMysqlDto> {
}
