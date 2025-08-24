package com.zsq.winter.es.util;

import org.elasticsearch.index.query.*;

import java.util.List;

/**
 * 查询构建器工具类
 * 
 * @author dadandiaoming
 */
public class QueryBuilderUtils {

    /**
     * 创建匹配查询
     */
    public static MatchQueryBuilder matchQuery(String field, Object value) {
        return QueryBuilders.matchQuery(field, value);
    }

    /**
     * 创建匹配短语查询
     */
    public static MatchPhraseQueryBuilder matchPhraseQuery(String field, String value) {
        return QueryBuilders.matchPhraseQuery(field, value);
    }

    /**
     * 创建多字段匹配查询
     */
    public static MultiMatchQueryBuilder multiMatchQuery(Object value, String... fields) {
        return QueryBuilders.multiMatchQuery(value, fields);
    }

    /**
     * 创建词条查询
     */
    public static TermQueryBuilder termQuery(String field, Object value) {
        return QueryBuilders.termQuery(field, value);
    }

    /**
     * 创建词条查询（多个值）
     */
    public static TermsQueryBuilder termsQuery(String field, Object... values) {
        return QueryBuilders.termsQuery(field, values);
    }

    /**
     * 创建范围查询
     */
    public static RangeQueryBuilder rangeQuery(String field) {
        return QueryBuilders.rangeQuery(field);
    }

    /**
     * 创建模糊查询
     */
    public static FuzzyQueryBuilder fuzzyQuery(String field, String value) {
        return QueryBuilders.fuzzyQuery(field, value);
    }

    /**
     * 创建通配符查询
     */
    public static WildcardQueryBuilder wildcardQuery(String field, String value) {
        return QueryBuilders.wildcardQuery(field, value);
    }

    /**
     * 创建前缀查询
     */
    public static PrefixQueryBuilder prefixQuery(String field, String value) {
        return QueryBuilders.prefixQuery(field, value);
    }

    /**
     * 创建存在查询
     */
    public static ExistsQueryBuilder existsQuery(String field) {
        return QueryBuilders.existsQuery(field);
    }

    /**
     * 创建布尔查询
     */
    public static BoolQueryBuilder boolQuery() {
        return QueryBuilders.boolQuery();
    }


} 