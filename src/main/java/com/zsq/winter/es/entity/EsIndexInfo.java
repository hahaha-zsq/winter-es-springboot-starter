package com.zsq.winter.es.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * es索引信息
 *
 * @author zhushengqian
 * @date 2025/08/24
 */

@Data
@Accessors(chain = true)
public class EsIndexInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 索引名称
     */
    private String indexName;

}
