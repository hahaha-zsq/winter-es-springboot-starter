package com.zsq.winter.es.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * es配置属性
 *
 * @author dadandiaoming
 */
@Data
@ConfigurationProperties(prefix = "winter-es")
public class EsConfigProperties {
    /**
     * es集群配置
     */
    private List<EsClusterConfig> esConfigs = new ArrayList<>();

    /**
     * 是否打印启动Banner
     */
    private Boolean isPrint = true;

    @Data
    public static class EsClusterConfig implements Serializable {

        /**
         * 集群名称
         */
        private String clusterName;

        /**
         * 节点地址列表
         */
        private List<String> hosts = new ArrayList<>();
        
        /**
         * 集群用户名
         */
        private String username;
        
        /**
         * 集群密码
         */
        private String password;
        
        /**
         * 连接超时时间（毫秒）
         */
        private Integer connectTimeout = 5000;
        
        /**
         * Socket超时时间（毫秒）
         */
        private Integer socketTimeout = 60000;
        
        /**
         * 连接请求超时时间（毫秒）
         */
        private Integer connectionRequestTimeout = 5000;

    }
}
