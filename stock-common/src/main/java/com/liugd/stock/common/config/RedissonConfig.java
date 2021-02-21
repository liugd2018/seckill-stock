package com.liugd.stock.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Configuration
public class RedissonConfig {


    @Resource
    RedisProperties redisProperties;

    public static final String NODE_PREFIX = "redis://";

    @Bean
    public RedissonClient redissonClint(){
        List<String> nodeList =  redisProperties.getCluster().getNodes();
        Config config = new Config();
        ClusterServersConfig clusterServersConfig = config.useClusterServers();
        for (String node : nodeList){
            clusterServersConfig.addNodeAddress(NODE_PREFIX.concat(node));
        }
        clusterServersConfig.setPassword(redisProperties.getPassword());
        return Redisson.create(config);
    }


}
