package com.liugd.stock.common.config;


import com.liugd.stock.common.constant.Constant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Configuration
public class RabbitConfig {


//    @Bean("customContainerFactory")
//    public SimpleRabbitListenerContainerFactory containerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        //设置线程数
//        factory.setConcurrentConsumers(10);
//        //最大线程数
//        factory.setMaxConcurrentConsumers(10);
//        configurer.configure(factory, connectionFactory);
//        return factory;
//    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 必须设置为 true，不然当 发送到交换器成功，但是没有匹配的队列，不会触发 ReturnCallback 回调
        // 而且 ReturnCallback 比 ConfirmCallback 先回调，意思就是 ReturnCallback 执行完了才会执行 ConfirmCallback
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     *
     * 锁库存同步mysql mq
     * @return
     */
    @Bean
    public Queue lockProductQueue(){
        return new Queue(Constant.RabbitQueue.LOCK_QUEUE_NAME);
    }

    @Bean
    public DirectExchange lockProductDirectExchange(){
        return new DirectExchange(Constant.RabbitQueue.LOCK_EXCHANGE_NAME);
    }

    @Bean
    public Binding lockProductBinding(){
        return  BindingBuilder.bind(lockProductQueue())
                .to(lockProductDirectExchange()).with(Constant.RabbitQueue.LOCK_ROUTING_KEY);
    }

    /**
     * 操作日志
     * @return
     */
    @Bean
    public Queue logQueue(){
        return new Queue(Constant.RabbitQueue.LOG_QUEUE_NAME);
    }

    @Bean
    public DirectExchange logDirectExchange(){
        return new DirectExchange(Constant.RabbitQueue.LOG_EXCHANGE_NAME);
    }

    @Bean
    public Binding logBinding(){
        return  BindingBuilder.bind(logQueue())
                .to(logDirectExchange()).with(Constant.RabbitQueue.LOG_ROUTING_KEY);
    }

    /**
     *
     * 解锁库存同步mysql mq
     * @return
     */
    @Bean
    public Queue unlockProductQueue(){
        return new Queue(Constant.RabbitQueue.UNLOCK_QUEUE_NAME);
    }

    @Bean
    public DirectExchange unlockProductDirectExchange(){
        return new DirectExchange(Constant.RabbitQueue.UNLOCK_EXCHANGE_NAME);
    }



    @Bean
    public Binding unlockProductBinding(){
        return  BindingBuilder.bind(unlockProductQueue())
                .to(unlockProductDirectExchange()).with(Constant.RabbitQueue.UNLOCK_ROUTING_KEY);
    }

    /**
     * 自动解锁延时队列
     * @return
     */
    @Bean
    public Queue autoUnlockDelayQueue(){
        Queue queue = new Queue(Constant.RabbitQueue.AUTO_UNLOCK_DELAY_QUEUE_NAME);
        queue.getArguments().put("x-dead-letter-exchange",Constant.RabbitQueue.AUTO_UNLOCK_EXCHANGE_NAME);
        queue.getArguments().put("x-dead-letter-routing-key", Constant.RabbitQueue.AUTO_UNLOCK_ROUTING_KEY);
        // 设置死ttl,需要修改ttl时间只能删除队列然后重建
//        queue.getArguments().put("x-message-ttl", TimeUnit.SECONDS.toMillis(15));
        return queue;
    }


    @Bean
    public DirectExchange autoUnlockDelayProductDirectExchange(){
        return new DirectExchange(Constant.RabbitQueue.AUTO_UNLOCK_DELAY_EXCHANGE_NAME);
    }

    @Bean
    public Binding autoUnlockDelayProductBinding(){
        return  BindingBuilder.bind(autoUnlockDelayQueue())
                .to(autoUnlockDelayProductDirectExchange())
                .with(Constant.RabbitQueue.AUTO_UNLOCK_DELAY_ROUTING_KEY);
    }

    /**
     *
     * 自动解锁队列 mq
     * @return
     */
    @Bean
    public Queue autoUnlockProductQueue(){
        return new Queue(Constant.RabbitQueue.AUTO_UNLOCK_QUEUE_NAME);
    }

    @Bean
    public DirectExchange autoUnlockProductDirectExchange(){
        return new DirectExchange(Constant.RabbitQueue.AUTO_UNLOCK_EXCHANGE_NAME);
    }

    @Bean
    public Binding autoUnlockProductBinding(){
        return  BindingBuilder.bind(autoUnlockProductQueue())
                .to(autoUnlockProductDirectExchange())
                .with(Constant.RabbitQueue.AUTO_UNLOCK_ROUTING_KEY);
    }

    /**
     *
     * 添加库存同步mysql
     * @return
     */
    @Bean
    public Queue syncAddProductQueue(){
        return new Queue(Constant.RabbitQueue.SYNC_ADD_PRODUCT_QUEUE_NAME);
    }

    @Bean
    public DirectExchange syncAddProductDirectExchange(){
        return new DirectExchange(Constant.RabbitQueue.SYNC_ADD_PRODUCT_EXCHANGE);
    }

    @Bean
    public Binding syncAddProductBinding(){
        return  BindingBuilder.bind(syncAddProductQueue())
                .to(syncAddProductDirectExchange())
                .with(Constant.RabbitQueue.SYNC_ADD_PRODUCT_ROUTING_KEY);
    }


    /**
     *
     * 更新库存同步mysql
     * @return
     */
    @Bean
    public Queue syncUpdateProductQueue(){
        return new Queue(Constant.RabbitQueue.SYNC_UPDATE_PRODUCT_QUEUE_NAME);
    }

    @Bean
    public DirectExchange syncUpdateProductDirectExchange(){
        return new DirectExchange(Constant.RabbitQueue.SYNC_UPDATE_PRODUCT_EXCHANGE);
    }

    @Bean
    public Binding syncUpdateProductBinding(){
        return  BindingBuilder.bind(syncUpdateProductQueue())
                .to(syncUpdateProductDirectExchange())
                .with(Constant.RabbitQueue.SYNC_UPDATE_PRODUCT_ROUTING_KEY);
    }


    /**
     * 清除本地缓存mq
     * @return
     */
    @Bean
    public FanoutExchange loadingCacheFanoutExchange(){
        return new FanoutExchange(Constant.RabbitQueue.CLEAN_LOADING_CACHE_EXCHANGE);
    }
}
