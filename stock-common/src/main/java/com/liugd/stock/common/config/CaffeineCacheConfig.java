package com.liugd.stock.common.config;

import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Slf4j
@Configuration
public class CaffeineCacheConfig {


    /**
     *  定义缓存,可直接使用
     * @return
     */
    @Bean
    public LoadingCache expiryCache(){
        LoadingCache<String, Object> loadingCache = Caffeine.newBuilder()
                // TODO 放到配置文件中
                .initialCapacity(100)
                // TODO 放到配置文件中
                .maximumSize(1000)
                //缓存写入/删除监控
                .writer(new CacheWriter<Object, Object>() {
                    @Override
                    public void write(Object key, Object value) { //此方法是同步阻塞的
                        log.info("--缓存写入--:key={}, value={}", key, value);
                    }
                    @Override
                    public void delete(Object key, Object value, RemovalCause cause) {
                        log.info("--缓存删除--:key={}",key);
                    }
                })
                //过期时间 TODO 放到配置文件中
                .expireAfterAccess(1, TimeUnit.MINUTES)
                // cache load实现类,刷新时候调用
                .build((String key)->"刷新的数据");
        return loadingCache;
    }
}
