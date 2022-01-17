package com.liugd.stock.common.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Getter
@Setter
public class LogHistoryDto {

    private String url;

    private long execTime;

    private int httpStatus;

    private String request;

    private String response;

}
