package com.liugd.stock.common.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Configuration
public class ElasticSearchConfig {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ElasticSearchFactoryProperties elasticSearchFactoryProperties;

    @Bean
    public RestHighLevelClient highLevelClient() {

        log.info("加载ES-CLIENT配置.");

        List<String> nodes = elasticSearchFactoryProperties.getNodes();

        if (CollectionUtils.isEmpty(nodes)) {
            throw new RuntimeException("加载ES-CLIENT配置失败.未获取到ES服务节点信息.");
        }

        HttpHost[] httpHosts = new HttpHost[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            // ip:port
            String[] address = nodes.get(i).split(":");
            httpHosts[i] = new HttpHost(address[0], Integer.parseInt(address[1]), elasticSearchFactoryProperties.getSchema());
        }

        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);

        return new RestHighLevelClient(restClientBuilder);
    }
}
