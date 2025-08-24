package com.zsq.winter.es.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * es文档实体
 *
 * @author zhushengqian
 * @date 2025/08/24
 */

@Data
@Accessors(chain = true)
public class EsDocData implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 文档id
     */
    private String docId;

    /**
     * 数据
     */
    private Map<String, Object> data;

}
