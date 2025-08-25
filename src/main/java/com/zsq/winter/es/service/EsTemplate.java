package com.zsq.winter.es.service;

import com.zsq.winter.es.client.EsRestClient;
import com.zsq.winter.es.entity.EsDocData;
import com.zsq.winter.es.entity.EsIndexInfo;
import com.zsq.winter.es.entity.EsSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 操作模板类
 * 提供文档的增删改查、搜索等操作，以及索引管理操作，类似于 Spring 的 JdbcTemplate
 *
 * @author dadandiaoming
 */
@Slf4j
public class EsTemplate {

    private final EsRestClient esRestClient;

    private static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

    public EsTemplate(EsRestClient esRestClient) {
        this.esRestClient = esRestClient;
    }

    // ==================== 文档操作（保持原有方法） ====================

    /**
     * 插入文档
     *
     * @param esIndexInfo 索引信息
     * @param esDocData   文档数据
     * @return 是否成功
     */
    public boolean insertDocument(EsIndexInfo esIndexInfo, EsDocData esDocData) {
        try {
            IndexRequest indexRequest = new IndexRequest(esIndexInfo.getIndexName()) // 设置索引名称
                    .source(esDocData.getData())  //设置要索引的文档数据和数据格式。
                    .id(esDocData.getDocId());  //设置要索引的文档ID。

            RestHighLevelClient client = esRestClient.getClient(esIndexInfo.getClusterName());
            // RestHighLevelClient.index(IndexRequest request, RequestOptions options): 向Elasticsearch服务器发送索引文档的请求。
            client.index(indexRequest, COMMON_OPTIONS);
            log.debug("Successfully inserted document with id: {}", esDocData.getDocId());
            return true;
        } catch (Exception e) {
            log.error("Failed to insert document with id: {}", esDocData.getDocId(), e);
            return false;
        }
    }

    /**
     * 批量插入文档
     *
     * @param esIndexInfo   索引信息
     * @param esDocDataList 文档数据列表
     * @return 是否成功
     */
    public boolean batchInsertDocuments(EsIndexInfo esIndexInfo, List<EsDocData> esDocDataList) {
        if (esDocDataList == null || esDocDataList.isEmpty()) {
            log.warn("Document list is empty, skipping batch insert");
            return true;
        }

        try {
            //BulkRequest是Elasticsearch Java客户端中用于批量操作的请求类，它允许您一次性发送多个索引、更新、删除等操作
            //add(IndexRequest request): 向批量请求中添加一个索引请求。
            //add(UpdateRequest request): 向批量请求中添加一个更新请求。
            //add(DeleteRequest request): 向批量请求中添加一个删除请求。
            //add(DocWriteRequest request): 向批量请求中添加一个文档写入请求（索引、更新或删除）。
            BulkRequest bulkRequest = new BulkRequest();
            esDocDataList.forEach(esDocData -> {
                IndexRequest indexRequest = new IndexRequest(esIndexInfo.getIndexName())
                        .source(esDocData.getData())
                        .id(esDocData.getDocId());
                bulkRequest.add(indexRequest);
            });

            RestHighLevelClient client = esRestClient.getClient(esIndexInfo.getClusterName());
            BulkResponse bulkResponse = client.bulk(bulkRequest, COMMON_OPTIONS);

            if (bulkResponse.hasFailures()) {
                log.error("Bulk insert has failures: {}", bulkResponse.buildFailureMessage());
                return false;
            }

            log.debug("Successfully inserted {} documents", esDocDataList.size());
            return true;
        } catch (Exception e) {
            log.error("Failed to batch insert documents", e);
            return false;
        }
    }

    /**
     * 更新文档
     *
     * @param esIndexInfo 索引信息
     * @param esDocData   文档数据
     * @return 是否成功
     */
    public boolean updateDocument(EsIndexInfo esIndexInfo, EsDocData esDocData) {
        try {
            // UpdateRequest类是Elasticsearch Java客户端中用于更新文档的请求类
            UpdateRequest updateRequest = new UpdateRequest()
                    .index(esIndexInfo.getIndexName())  // 设置要更新的文档所在的索引名称
                    .id(esDocData.getDocId()) // 设置要更新的文档的ID。
                    .doc(esDocData.getData()); // 设置要更新的文档的新内容，使用Map来表示文档内容。

            RestHighLevelClient client = esRestClient.getClient(esIndexInfo.getClusterName());
            client.update(updateRequest, COMMON_OPTIONS);
            log.debug("Successfully updated document with id: {}", esDocData.getDocId());
            return true;
        } catch (Exception e) {
            log.error("Failed to update document with id: {}", esDocData.getDocId(), e);
            return false;
        }
    }

    /**
     * 批量更新文档
     *
     * @param esIndexInfo   索引信息
     * @param esDocDataList 文档数据列表
     * @return 是否成功
     */
    public boolean batchUpdateDocuments(EsIndexInfo esIndexInfo, List<EsDocData> esDocDataList) {
        if (esDocDataList == null || esDocDataList.isEmpty()) {
            log.warn("Document list is empty, skipping batch update");
            return true;
        }

        try {
            BulkRequest bulkRequest = new BulkRequest();
            boolean hasValidRequests = false;

            for (EsDocData esDocData : esDocDataList) {
                if (!ObjectUtils.isEmpty(esDocData.getDocId())) {
                    UpdateRequest updateRequest = new UpdateRequest()
                            .index(esIndexInfo.getIndexName())
                            .id(esDocData.getDocId())
                            .doc(esDocData.getData());
                    bulkRequest.add(updateRequest);
                    hasValidRequests = true;
                }
            }

            if (!hasValidRequests) {
                log.error("No valid document IDs found for batch update");
                return true;
            }

            RestHighLevelClient client = esRestClient.getClient(esIndexInfo.getClusterName());
            BulkResponse bulkResponse = client.bulk(bulkRequest, COMMON_OPTIONS);

            if (bulkResponse.hasFailures()) {
                log.error("Bulk update has failures: {}", bulkResponse.buildFailureMessage());
                return false;
            }

            log.debug("Successfully updated {} documents", esDocDataList.size());
            return true;
        } catch (Exception e) {
            log.error("Failed to batch update documents", e);
            return false;
        }
    }

    /**
     * 删除单个文档
     *
     * @param esIndexInfo 索引信息
     * @param docId       文档ID
     * @return 是否成功
     */
    public boolean deleteDocument(EsIndexInfo esIndexInfo, String docId) {
        try {
            DeleteRequest deleteRequest = new DeleteRequest()
                    .index(esIndexInfo.getIndexName())
                    .id(docId);

            RestHighLevelClient client = esRestClient.getClient(esIndexInfo.getClusterName());
            DeleteResponse response = client.delete(deleteRequest, COMMON_OPTIONS);
            log.debug("Successfully deleted document with id: {}, result: {}", docId, response.getResult());
            return true;
        } catch (Exception e) {
            log.error("Failed to delete document with id: {}", docId, e);
            return false;
        }
    }

    /**
     * 批量删除文档
     *
     * @param esIndexInfo 索引信息
     * @param docIdList   文档ID列表
     * @return 是否成功
     */
    public boolean batchDeleteDocuments(EsIndexInfo esIndexInfo, List<String> docIdList) {
        if (ObjectUtils.isEmpty(docIdList)) {
            log.warn("Document ID list is empty, skipping batch delete");
            return true;
        }

        try {
            BulkRequest bulkRequest = new BulkRequest();
            docIdList.forEach(docId ->
                    bulkRequest.add(new DeleteRequest().index(esIndexInfo.getIndexName()).id(docId))
            );

            RestHighLevelClient client = esRestClient.getClient(esIndexInfo.getClusterName());
            BulkResponse bulkResponse = client.bulk(bulkRequest, COMMON_OPTIONS);

            if (bulkResponse.hasFailures()) {
                log.error("Bulk delete has failures: {}", bulkResponse.buildFailureMessage());
                return false;
            }

            log.debug("Successfully deleted {} documents", docIdList.size());
            return true;
        } catch (Exception e) {
            log.error("Failed to batch delete documents", e);
            return false;
        }
    }

    /**
     * 删除所有文档
     *
     * @param esIndexInfo 索引信息
     * @return 删除的文档数量
     */
    public long deleteAllDocuments(EsIndexInfo esIndexInfo) {
        try {
            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(esIndexInfo.getIndexName())
                    .setQuery(QueryBuilders.matchAllQuery());

            RestHighLevelClient client = esRestClient.getClient(esIndexInfo.getClusterName());
            BulkByScrollResponse response = client.deleteByQuery(deleteByQueryRequest, COMMON_OPTIONS);

            long deleted = response.getDeleted();
            log.info("Successfully deleted {} documents from index: {}", deleted, esIndexInfo.getIndexName());
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete all documents from index: {}", esIndexInfo.getIndexName(), e);
            return 0;
        }
    }

    /**
     * 检查文档是否存在
     *
     * @param esIndexInfo 索引信息
     * @param docId       文档ID
     * @return 是否存在
     */
    public boolean documentExists(EsIndexInfo esIndexInfo, String docId) {
        try {
            GetRequest getRequest = new GetRequest()
                    .index(esIndexInfo.getIndexName())
                    .id(docId);

            RestHighLevelClient client = esRestClient.getClient(esIndexInfo.getClusterName());
            return client.exists(getRequest, COMMON_OPTIONS);
        } catch (Exception e) {
            log.error("Failed to check document existence for id: {}", docId, e);
            return false;
        }
    }

    /**
     * 根据ID获取文档
     *
     * @param esIndexInfo 索引信息
     * @param docId       文档ID
     * @return 文档数据
     */
    public Map<String, Object> getDocumentById(EsIndexInfo esIndexInfo, String docId) {
        try {
            GetRequest getRequest = new GetRequest()
                    .index(esIndexInfo.getIndexName())
                    .id(docId);

            RestHighLevelClient client = esRestClient.getClient(esIndexInfo.getClusterName());
            GetResponse response = client.get(getRequest, COMMON_OPTIONS);
            return response.getSource();
        } catch (Exception e) {
            log.error("Failed to get document with id: {}", docId, e);
            return null;
        }
    }

    /**
     * 根据ID获取文档的指定字段
     *
     * @param esIndexInfo 索引信息
     * @param docId       文档ID
     * @param fields      字段列表
     * @return 文档数据
     */
    public Map<String, Object> getDocumentById(EsIndexInfo esIndexInfo, String docId, String[] fields) {
        try {
            GetRequest getRequest = new GetRequest()
                    .index(esIndexInfo.getIndexName())
                    .id(docId)
                    .fetchSourceContext(new FetchSourceContext(true, fields, null));

            RestHighLevelClient client = esRestClient.getClient(esIndexInfo.getClusterName());
            GetResponse response = client.get(getRequest, COMMON_OPTIONS);
            return response.getSource();
        } catch (Exception e) {
            log.error("Failed to get document with id: {} and fields: {}", docId, fields, e);
            return null;
        }
    }

    /**
     * 搜索文档
     *
     * @param esIndexInfo     索引信息
     * @param esSearchRequest 搜索请求
     * @return 搜索响应
     */
    public SearchResponse searchDocuments(EsIndexInfo esIndexInfo, EsSearchRequest esSearchRequest) {
        try {
            SearchSourceBuilder searchSourceBuilder = buildSearchSourceBuilder(esSearchRequest);
              /*SearchRequest是Elasticsearch中的一个Java API，用于向Elasticsearch发送搜索请求。
            它允许用户构建一个搜索请求，指定要搜索的索引、类型、查询条件、排序方式、高亮显示、聚合操作等，并发送给Elasticsearch进行搜索*/
            SearchRequest searchRequest = buildSearchRequest(esIndexInfo, esSearchRequest, searchSourceBuilder);

            RestHighLevelClient client = esRestClient.getClient(esIndexInfo.getClusterName());
            return client.search(searchRequest, COMMON_OPTIONS);
        } catch (Exception e) {
            log.error("Failed to search documents in index: {}", esIndexInfo.getIndexName(), e);
            return null;
        }
    }

    /**
     * 构建搜索源构建器
     */
    private SearchSourceBuilder buildSearchSourceBuilder(EsSearchRequest esSearchRequest) {
        /*  SearchSourceBuilder类是用于构建搜索请求的一部分，具体来说，SearchSourceBuilder的作用包括：
            设置查询条件：可以定义各种类型的查询，如match查询、term查询、range查询等。
            设置过滤条件：可以定义过滤条件，用于限定搜索结果。
            设置排序规则：可以指定按照某个字段进行升序或降序排序。
            设置分页参数：可以指定从搜索结果中的哪个位置开始获取数据以及获取多少条数据。
            添加聚合操作：可以定义各种类型的聚合操作，如term聚合、range聚合、嵌套聚合等。
            控制返回的字段：可以指定只返回文档的部分字段，而不是全部字段。
         */
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 设置查询条件
        if (esSearchRequest.getBq() != null) {
            searchSourceBuilder.query(esSearchRequest.getBq());
        }

        // 设置返回字段
        if (esSearchRequest.getFields() != null) {
            searchSourceBuilder.fetchSource(esSearchRequest.getFields(), null);
        }

        // 设置分页
        searchSourceBuilder.from(esSearchRequest.getFrom());
        searchSourceBuilder.size(esSearchRequest.getSize());

        // 设置高亮
        if (esSearchRequest.getHighlightBuilder() != null) {
            searchSourceBuilder.highlighter(esSearchRequest.getHighlightBuilder());
        }

        // 设置排序
        if (!ObjectUtils.isEmpty(esSearchRequest.getSortName())) {
            searchSourceBuilder.sort(esSearchRequest.getSortName());
        }

        // 默认按评分排序
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));

        return searchSourceBuilder;
    }

    /**
     * 构建搜索请求
     */
    private SearchRequest buildSearchRequest(EsIndexInfo esIndexInfo, EsSearchRequest esSearchRequest,
                                             SearchSourceBuilder searchSourceBuilder) {
        /*  searchType 是用于指定搜索操作类型的参数
            1:query_then_fetch：首先执行查询操作，然后获取匹配的文档，适用于大多数搜索场景。默认
            2:dfs_query_then_fetch：在分布式环境中使用分布式频率（DFS）来执行查询，适用于特定的分布式搜索场景。
            3:count：仅返回匹配的文档数量，而不返回实际的文档内容。
            4:scan：用于旧版本的滚动搜索，已经在较新的版本中废弃。*/
        SearchRequest searchRequest = new SearchRequest()
                .indices(esIndexInfo.getIndexName())
                .searchType(SearchType.DEFAULT)
                .source(searchSourceBuilder); // // source(SearchSourceBuilder source)：设置搜索的源，可以包括查询条件、排序规则、分页设置等。

        // 设置滚动搜索
        if (esSearchRequest.getNeedScroll() != null && esSearchRequest.getNeedScroll()) {
            Scroll scroll = new Scroll(TimeValue.timeValueMinutes(esSearchRequest.getMinutes()));
            searchRequest.scroll(scroll);
        }

        return searchRequest;
    }

    /**
     * 获取所有集群名称
     *
     * @return 集群名称列表
     */
    public List<String> getClusterNames() {
        return esRestClient.getClusterNames();
    }

    /**
     * 检查集群连接状态
     *
     * @param clusterName 集群名称
     * @return 是否连接
     */
    public boolean isClusterConnected(String clusterName) {
        return esRestClient.isClusterConnected(clusterName);
    }
}
