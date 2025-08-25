package com.zsq.winter.es.config;

import com.zsq.winter.es.client.EsRestClient;
import com.zsq.winter.es.entity.BannerCreator;
import com.zsq.winter.es.entity.EsConfigProperties;
import com.zsq.winter.es.service.EsTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 自动配置类
 * 
 * @author dadandiaoming
 */
@Configuration
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
    @ConditionalOnMissingBean
    public EsTemplate esTemplate(EsRestClient esRestClient) {
        return new EsTemplate(esRestClient);
    }

    /**
     * 创建启动Banner创建器Bean
     *
     * <p>用于在应用启动时输出定制的banner</p>
     *
     * @param esConfigProperties 包含es相关的配置属性
     * @return 初始化后的BannerCreator实例
     * @see BannerCreator
     */
    @Bean("esBanner")
    public BannerCreator bannerCreator(EsConfigProperties esConfigProperties) {
        return new BannerCreator(esConfigProperties);
    }

} 