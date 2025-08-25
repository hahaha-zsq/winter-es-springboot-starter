package com.zsq.winter.es.config;

import com.zsq.winter.es.client.EsRestClient;
import com.zsq.winter.es.entity.EsConfigProperties;
import com.zsq.winter.es.service.EsTemplate;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Elasticsearch 自动配置类
 * 
 * @author dadandiaoming
 */
@Configuration
@ConditionalOnClass(RestHighLevelClient.class)
@ConditionalOnProperty(prefix = "winter-es", name = "es-configs")
@EnableConfigurationProperties(EsConfigProperties.class)
public class ElasticsearchAutoConfiguration {

    /**
     * 配置 Elasticsearch 客户端
     * 
     * @param esConfigProperties ES配置属性
     * @return ES客户端实例
     */
    @Bean
    @ConditionalOnMissingBean
    public EsRestClient esRestClient(EsConfigProperties esConfigProperties) {
        return new EsRestClient(esConfigProperties);
    }

    /**
     * 配置 Elasticsearch 操作模板
     * 
     * @param esRestClient ES客户端
     * @return ES操作模板实例
     */
    @Bean
    @DependsOn(value="esRestClient")
    @ConditionalOnMissingBean
    public EsTemplate esTemplate(EsRestClient esRestClient) {
        return new EsTemplate(esRestClient);
    }
} 