package com.liugd.stock.listener.listener;

import com.liugd.stock.common.constant.Constant;
import com.liugd.stock.common.dto.LogHistoryDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Component
public class LogListener {




    @RabbitListener(queues = Constant.RabbitQueue.LOG_QUEUE_NAME)
    public void logHistory(LogHistoryDto logHistoryDto){



    }

}
