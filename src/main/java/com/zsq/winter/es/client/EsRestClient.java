package com.zsq.winter.es.client;

import com.zsq.winter.es.entity.EsConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 客户端初始化类
 * 负责创建和管理 Elasticsearch 客户端连接
 *
 * @author dadandiaoming
 */
@Slf4j
public class EsRestClient {

    /**
     * 客户端映射表
     */
    private final Map<String, RestHighLevelClient> clientMap = new HashMap<>();

    private final EsConfigProperties esConfigProperties;

    /**
     * 构造函数
     */
    public EsRestClient(EsConfigProperties esConfigProperties) {
        this.esConfigProperties = esConfigProperties;
    }

    /**
     * 初始化客户端连接
     */
    @PostConstruct
    public void initialize() {
        List<EsConfigProperties.EsClusterConfig> esConfigs = esConfigProperties.getEsConfigs();
        if (ObjectUtils.isEmpty(esConfigs)) {
            log.warn("No Elasticsearch cluster configuration found");
            return;
        }

        for (EsConfigProperties.EsClusterConfig esConfig : esConfigs) {
            try {
                log.info("Initializing Elasticsearch client for cluster: {}, hosts: {}",
                        esConfig.getClusterName(), esConfig.getHosts());
                RestHighLevelClient client = createRestClient(esConfig);
                if (!ObjectUtils.isEmpty(client)) {
                    clientMap.put(esConfig.getClusterName(), client);
                    log.info("Successfully initialized client for cluster: {}", esConfig.getClusterName());
                }
            } catch (Exception e) {
                log.error("Failed to initialize client for cluster: {}, hosts: {}",
                        esConfig.getClusterName(), esConfig.getHosts(), e);
            }
        }
    }

    /**
     * 销毁客户端连接
     */
    @PreDestroy
    public void destroy() {
        clientMap.values().forEach(client -> {
            try {
                client.close();
            } catch (IOException e) {
                log.error("Error closing Elasticsearch client", e);
            }
        });
        clientMap.clear();
        log.info("All Elasticsearch clients have been closed");
    }

    /**
     * 创建 RestHighLevelClient
     */
    private RestHighLevelClient createRestClient(EsConfigProperties.EsClusterConfig esClusterConfig) {
        // 获取主机地址列表
        List<String> hosts = esClusterConfig.getHosts();
        if (ObjectUtils.isEmpty(hosts)) {
            throw new RuntimeException("No valid hosts found in configuration for cluster: " + esClusterConfig.getClusterName());
        }

        List<HttpHost> httpHostList = new ArrayList<>(hosts.size());

        for (String host : hosts) {
            String trimmedHost = host.trim();
            if (!ObjectUtils.isEmpty(trimmedHost)) {
                HttpHost httpHost = HttpHost.create("http://" + trimmedHost);
                httpHostList.add(httpHost);
            }
        }

        if (httpHostList.isEmpty()) {
            throw new RuntimeException("No valid hosts found in configuration: " + hosts);
        }

        HttpHost[] httpHosts = httpHostList.toArray(new HttpHost[0]);

        // 配置认证信息
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (!ObjectUtils.isEmpty(esClusterConfig.getUsername()) &&
                !ObjectUtils.isEmpty(esClusterConfig.getPassword())) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(esClusterConfig.getUsername(), esClusterConfig.getPassword()));
        }

        // 构建客户端
        RestClientBuilder builder = RestClient.builder(httpHosts)
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .setRequestConfigCallback(requestConfigBuilder ->
                        requestConfigBuilder
                                .setConnectTimeout(esClusterConfig.getConnectTimeout())
                                .setSocketTimeout(esClusterConfig.getSocketTimeout())
                                .setConnectionRequestTimeout(esClusterConfig.getConnectionRequestTimeout()));

        return new RestHighLevelClient(builder);
    }

    /**
     * 获取指定集群的客户端
     */
    public RestHighLevelClient getClient(String clusterName) {
        RestHighLevelClient client = clientMap.get(clusterName);
        if (ObjectUtils.isEmpty(client)) {
            throw new RuntimeException("Elasticsearch client not found for cluster: " + clusterName);
        }
        return client;
    }

    /**
     * 获取所有集群名称
     */
    public List<String> getClusterNames() {
        return new ArrayList<>(clientMap.keySet());
    }

    /**
     * 检查集群连接状态
     */
    public boolean isClusterConnected(String clusterName) {
        try {
            RestHighLevelClient client = getClient(clusterName);
            return client.ping(org.elasticsearch.client.RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("Failed to ping cluster: {}", clusterName, e);
            return false;
        }
    }
}