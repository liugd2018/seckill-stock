package com.liugd.stock.common.es;

import com.liugd.stock.common.exception.BusinessException;
import com.liugd.stock.common.utils.JsonUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Component
public class EsRepository {

    protected Logger log = LoggerFactory.getLogger(this.getClass());


    @Resource
    RestHighLevelClient restHighLevelClient;

    /**
     * 查看索引是否存在
     *
     * @param indexName 索引名
     * @return 创建状态
     * @throws IOException 异常
     */
    public boolean existIndex(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * @param indexName        索引名字
     * @param mappingInfo      mapping
     * @param numberOfShards   分片
     * @param numberOfReplicas 备份
     * @throws IOException
     */
    public boolean createIndex(String indexName, String mappingInfo,
                               int numberOfShards, int numberOfReplicas) throws IOException {
        if (!existIndex(indexName)) {
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            // settings部分
            request.settings(Settings.builder()
                    // 创建索引时，分配的主分片的数量
                    .put("index.number_of_shards", numberOfShards)
                    // 创建索引时，为每一个主分片分配的副本分片的数量
                    .put("index.number_of_replicas", numberOfReplicas)
            );
            // mapping部分 除了用json字符串来定义外，还可以使用Map或者XContentBuilder
            request.mapping(mappingInfo, XContentType.JSON);
            // 创建索引(同步的方式)
            CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            return checkResponse(response);

        } else {
            return false;
        }
    }

    /**
     * check ES请求是否成功
     *
     * @param acknowledgedResponse 返回数据
     * @return {@link AcknowledgedResponse#isAcknowledged()} true if the response is acknowledged, false otherwise
     */
    boolean checkResponse(AcknowledgedResponse acknowledgedResponse) {

        if (Objects.isNull(acknowledgedResponse)) {
            return false;
        }
        return acknowledgedResponse.isAcknowledged();
    }


    /**
     * 新增document common
     *
     * @param param
     * @param indexRequest
     * @return
     * @throws IOException
     */
    private boolean addDocument(Object param, IndexRequest indexRequest) throws IOException {
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        if (Objects.isNull(indexResponse)) {
            return false;
        }

        if ((indexResponse.status().getStatus() != RestStatus.OK.getStatus())
                && (indexResponse.status().getStatus() != RestStatus.CREATED.getStatus())) {
            log.warn("创建Document 失败. index:{} DocumentID:{} status:{} mapping:{} indexResponse:{}",
                    indexResponse.getIndex(), indexResponse.getId(), indexResponse.status().getStatus(),
                    JsonUtil.toStringNoException(param), JsonUtil.toStringNoException(indexResponse));
            return false;
        }

        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            //处理(如果需要)第一次创建文档的情况
            log.info("创建Document. index:{} DocumentID:{} mapping:{} indexResponse:{}",
                    indexResponse.getIndex(), indexResponse.getId(),
                    JsonUtil.toStringNoException(param), JsonUtil.toStringNoException(indexResponse));
        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            //处理(如果需要)将文档重写为已经存在的情况
            log.info("更新已有的Document. index:{} DocumentID:{} mapping:{} indexResponse:{}",
                    indexResponse.getIndex(), indexResponse.getId(),
                    JsonUtil.toStringNoException(param), JsonUtil.toStringNoException(indexResponse));
        } else {
            log.info("更新已有的Document. index:{} DocumentID:{} mapping:{} indexResponse:{}",
                    indexResponse.getIndex(), indexResponse.getId(),
                    JsonUtil.toStringNoException(param), JsonUtil.toStringNoException(indexResponse));
        }
        return true;
    }

    /**
     * 添加别名
     *
     * @param indexName
     * @param aliasName
     * @return
     * @throws IOException
     */
    public boolean addAlias(String indexName, String aliasName) throws IOException {
        IndicesAliasesRequest request = new IndicesAliasesRequest();
        IndicesAliasesRequest.AliasActions aliasAction = new IndicesAliasesRequest
                .AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                .index(indexName)
                .alias(aliasName);
        request.addAliasAction(aliasAction);

        AcknowledgedResponse indicesAliasesResponse =
                restHighLevelClient.indices().updateAliases(request, RequestOptions.DEFAULT);

        return checkResponse(indicesAliasesResponse);
    }

    /**
     * 删除index 别名
     *
     * @param indexName index name
     * @param aliasName 别名
     * @return 是否成功
     * @throws IOException
     */
    public boolean removeAlias(String aliasName, String indexName) throws IOException {

        IndicesAliasesRequest request = new IndicesAliasesRequest();
        IndicesAliasesRequest.AliasActions aliasAction = new IndicesAliasesRequest
                .AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                .alias(indexName)
                .alias(aliasName);
        request.addAliasAction(aliasAction);

        AcknowledgedResponse indicesAliasesResponse =
                restHighLevelClient.indices().updateAliases(request, RequestOptions.DEFAULT);

        return checkResponse(indicesAliasesResponse);
    }

    /**
     * 重置别名 (原子操作)
     *
     * @param aliasName 别名
     * @param indexName index name
     * @return
     * @throws IOException
     */
    public boolean changAlias(String aliasName, String indexName) throws IOException {

        IndicesAliasesRequest request = new IndicesAliasesRequest();
        // 添加别名
        IndicesAliasesRequest.AliasActions aliasActionAdd = new IndicesAliasesRequest
                .AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                .index(indexName)
                .alias(aliasName);
        request.addAliasAction(aliasActionAdd);

        // 获取别名下的index、解除别名的绑定
        Map<String, Set<AliasMetaData>> aliases = getAliases(aliasName);
        aliases.forEach((key, aliasMetaDataSet) -> {
            IndicesAliasesRequest.AliasActions aliasActionRemove = new IndicesAliasesRequest
                    .AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                    .index(key)
                    .alias(aliasName);
            request.addAliasAction(aliasActionRemove);
        });
        AcknowledgedResponse indicesAliasesResponse =
                restHighLevelClient.indices().updateAliases(request, RequestOptions.DEFAULT);

        return checkResponse(indicesAliasesResponse);
    }


    /**
     * 获取别名
     *
     * @param aliasName 别名
     * @return 别名下的全部index
     * @throws IOException
     */
    public Map<String, Set<AliasMetaData>> getAliases(String aliasName) throws IOException {
        GetAliasesRequest request = new GetAliasesRequest().aliases(aliasName);
        GetAliasesResponse response = restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);

        return response.getAliases();
    }


    /**
     * 查询es共通
     *
     * @param indexName
     * @param sourceBuilder
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> List<T> searchDocumentCommon(String indexName, SearchSourceBuilder sourceBuilder,
                                     Class<T> clazz) throws Exception {
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        // 同步的方式发送请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<T> list = searchResolve(clazz, searchResponse);
        return list;
    }

    <T> Pair<List<T>, Long> searchDocumentCommonPage(String indexName, SearchSourceBuilder sourceBuilder,
                                                     Class<T> clazz) throws Exception {
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        // 同步的方式发送请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        TotalHits totalHits = searchResponse.getHits().getTotalHits();
        ;

        List<T> list = searchResolve(clazz, searchResponse);


        return Pair.of(list, totalHits.value);
    }

    <T> List<T> searchResolve(Class<T> clazz, SearchResponse searchResponse) {
        if (Objects.isNull(searchResponse) || Objects.isNull(searchResponse.getHits())) {
            log.warn("查询ES失败. searchResponse:{}", JsonUtil.toStringNoException(searchResponse));
            throw new BusinessException(1, "查询ES失败.");
        }
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        List<T> list = new ArrayList<>();
        esResultConvert(clazz, searchHits, list);
        return list;
    }

    /**
     * es 返回结构转换
     *
     * @param clazz
     * @param searchHits
     * @param list
     * @param <T>
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private <T> void esResultConvert(Class<T> clazz, SearchHit[] searchHits, List<T> list) {
        for (SearchHit searchHit : searchHits) {
            list.add(JsonUtil.parseObjectNoException(searchHit.getSourceAsString(), clazz));
        }
    }

}
