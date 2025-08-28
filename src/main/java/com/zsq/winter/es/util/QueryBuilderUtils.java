package com.zsq.winter.es.util;

import org.elasticsearch.index.query.*;

/**
 * 查询构建器工具类
 *
 * @author dadandiaoming
 */
public class QueryBuilderUtils {

    /**
     * 创建匹配查询（Match Query）。
     * <p>
     * <p><b>工作原理：</b>对文本字段执行全文检索，按字段映射的分词器对查询值分词，与倒排索引中的词项匹配并按相关性评分。</p>
     * <p><b>使用场景：</b>自然语言检索、标题/正文模糊匹配、需要相关性排序的搜索。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // 查询 title 文本包含 "spring elasticsearch" 的文档
     * QueryBuilder qb = QueryBuilderUtils.matchQuery("title", "spring elasticsearch");
     * </pre>
     *
     * @param field 字段名
     * @param value 查询值（将按字段映射对应的分析器进行分词）
     * @return {@link MatchQueryBuilder} 用于构建 match 查询的构建器
     */
    public static MatchQueryBuilder matchQuery(String field, Object value) {
        return QueryBuilders.matchQuery(field, value);
    }

    /**
     * 创建匹配短语查询（Match Phrase Query）。
     * <p>
     * <p><b>工作原理：</b>分词后要求词项按相同顺序、在允许的间距（slop）内出现，以满足短语级别匹配。</p>
     * <p><b>使用场景：</b>句子/关键词组的连续匹配、强调词序的检索。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // 匹配描述中按短语顺序出现的 "error handling"
     * QueryBuilder qb = QueryBuilderUtils.matchPhraseQuery("description", "error handling");
     * </pre>
     *
     * @param field 字段名
     * @param value 短语文本
     * @return {@link MatchPhraseQueryBuilder} 用于构建 match_phrase 查询的构建器
     */
    public static MatchPhraseQueryBuilder matchPhraseQuery(String field, String value) {
        return QueryBuilders.matchPhraseQuery(field, value);
    }

    /**
     * 创建多字段匹配查询（Multi Match Query）。
     * <p>
     * <p><b>工作原理：</b>将同一查询值应用到多个字段进行全文匹配，聚合不同字段的评分得到总相关性。</p>
     * <p><b>使用场景：</b>跨标题/摘要/正文等多个文本字段的统一检索。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // 同时在 title、content 上匹配 "monitoring"
     * QueryBuilder qb = QueryBuilderUtils.multiMatchQuery("monitoring", "title", "content");
     * </pre>
     *
     * @param value  查询值（通常为文本）
     * @param fields 多个字段名
     * @return {@link MultiMatchQueryBuilder} 用于构建 multi_match 查询的构建器
     */
    public static MultiMatchQueryBuilder multiMatchQuery(Object value, String... fields) {
        return QueryBuilders.multiMatchQuery(value, fields);
    }

    /**
     * 创建词条查询（Term Query）。
     * <p>
     * <p><b>工作原理：</b>对未分词的精确值执行等值匹配，不进行分词；适用于 keyword、数值、日期等精确字段，类似于 MySQL 的 '='。</p>
     * <p><b>使用场景：</b>ID 精确匹配、状态码过滤、keyword 字段的精确查找。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // 精确匹配 keyword 字段 status = "ACTIVE"
     * QueryBuilder qb = QueryBuilderUtils.termQuery("status", "ACTIVE");
     * </pre>
     *
     * @param field 字段名
     * @param value 精确值（不分词）
     * @return {@link TermQueryBuilder} 用于构建 term 查询的构建器
     */
    public static TermQueryBuilder termQuery(String field, Object value) {
        return QueryBuilders.termQuery(field, value);
    }

    /**
     * 创建词条查询（多个值）（Terms Query）。
     * <p>
     * <p><b>工作原理：</b>匹配字段值属于给定集合中任一值的文档，不分词；类似于 MySQL 的 IN。</p>
     * <p><b>使用场景：</b>批量 ID 过滤、多状态过滤。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // 匹配 id in (1,2,3)
     * QueryBuilder qb = QueryBuilderUtils.termsQuery("id", 1, 2, 3);
     * </pre>
     *
     * @param field  字段名
     * @param values 多个精确值
     * @return {@link TermsQueryBuilder} 用于构建 terms 查询的构建器
     */
    public static TermsQueryBuilder termsQuery(String field, Object... values) {
        return QueryBuilders.termsQuery(field, values);
    }

    /**
     * 创建范围查询（Range Query）。
     * <p>
     * <p><b>工作原理：</b>基于有序类型（数值/日期等）进行区间匹配，可链式设置 gte/lte/gt/lt 等边界条件。</p>
     * <p><b>使用场景：</b>时间区间过滤、价格/分数阈值筛选。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // 时间范围：timestamp >= now-1d 且 < now
     * RangeQueryBuilder qb = QueryBuilderUtils.rangeQuery("timestamp").gte("now-1d").lt("now");
     * </pre>
     *
     * @param field 字段名
     * @return {@link RangeQueryBuilder} 用于构建 range 查询的构建器
     */
    public static RangeQueryBuilder rangeQuery(String field) {
        return QueryBuilders.rangeQuery(field);
    }

    /**
     * 创建模糊查询（Fuzzy Query）。
     * <p>
     * <p><b>工作原理：</b>基于编辑距离（Levenshtein Distance）允许一定数量的字符误差（插入/删除/替换）进行近似匹配。</p>
     * <p><b>使用场景：</b>用户拼写错误容错、名称近似匹配、纠错搜索。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // "fuxzy" 也可匹配到 "fuzzy"
     * QueryBuilder qb = QueryBuilderUtils.fuzzyQuery("name", "fuxzy");
     * </pre>
     *
     * @param field 字段名
     * @param value 文本值
     * @return {@link FuzzyQueryBuilder} 用于构建 fuzzy 查询的构建器
     */
    public static FuzzyQueryBuilder fuzzyQuery(String field, String value) {
        return QueryBuilders.fuzzyQuery(field, value);
    }

    /**
     * 创建正则查询（Regexp Query）。
     * <p>
     * <p><b>工作原理：</b>基于 Lucene 正则语法对未分词字段执行正则匹配，常用于 keyword 字段；性能取决于模式复杂度。</p>
     * <p><b>使用场景：</b>需要复杂模式匹配的场景，如代码/标识符/路径的规则校验。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // 匹配以 log-开头，结尾为数字的 keyword，如 log-001
     * QueryBuilder qb = QueryBuilderUtils.regexpQuery("tag.keyword", "log-\\d+");
     * </pre>
     *
     * @param field   字段名（通常为 keyword）
     * @param pattern 正则表达式（Lucene Regexp）
     * @return {@link RegexpQueryBuilder} 用于构建 regexp 查询的构建器
     */
    public static RegexpQueryBuilder regexpQuery(String field, String pattern) {
        return QueryBuilders.regexpQuery(field, pattern);
    }

    /**
     * 创建通配符查询（Wildcard Query）。
     * <p>
     * <p><b>工作原理：</b>基于通配符模式匹配，'*' 表示任意长度字符，'?' 表示单个字符；在大数据量时可能较慢。</p>
     * <p><b>使用场景：</b>keyword 字段的模式匹配、后缀未知的轻度模糊检索。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // 匹配以 "error-" 开头的 keyword：如 error-1, error-abc
     * QueryBuilder qb = QueryBuilderUtils.wildcardQuery("tag.keyword", "error-*");
     * </pre>
     *
     * @param field 字段名
     * @param value 通配符表达式（如 "abc*", "a?c"）
     * @return {@link WildcardQueryBuilder} 用于构建 wildcard 查询的构建器
     */
    public static WildcardQueryBuilder wildcardQuery(String field, String value) {
        return QueryBuilders.wildcardQuery(field, value);
    }

    /**
     * 创建前缀查询（Prefix Query）。
     * <p>
     * <p><b>工作原理：</b>匹配以给定前缀开头的字符串，通常用于未分词字段；性能优于大范围 wildcard。</p>
     * <p><b>使用场景：</b>前缀自动补全、按编号/编码前缀分组检索。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // 匹配 code 以 "ABC" 开头
     * QueryBuilder qb = QueryBuilderUtils.prefixQuery("code", "ABC");
     * </pre>
     *
     * @param field 字段名
     * @param value 前缀字符串
     * @return {@link PrefixQueryBuilder} 用于构建 prefix 查询的构建器
     */
    public static PrefixQueryBuilder prefixQuery(String field, String value) {
        return QueryBuilders.prefixQuery(field, value);
    }

    /**
     * 创建存在查询（Exists Query）。
     * <p>
     * <p><b>工作原理：</b>判断文档中是否存在指定字段（字段被索引且有值）。</p>
     * <p><b>使用场景：</b>过滤掉缺失字段的文档、校验数据完整性。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // 过滤存在 description 字段的文档
     * QueryBuilder qb = QueryBuilderUtils.existsQuery("description");
     * </pre>
     *
     * @param field 字段名
     * @return {@link ExistsQueryBuilder} 用于构建 exists 查询的构建器
     */
    public static ExistsQueryBuilder existsQuery(String field) {
        return QueryBuilders.existsQuery(field);
    }

    /**
     * 创建布尔查询（Bool Query）。
     * <p>
     * <p><b>工作原理：</b>组合多个子查询，支持 must（与）、should（或）、must_not（非）、filter（不参与评分）。</p>
     * <p><b>使用场景：</b>构建复杂检索逻辑、权重组合、精准过滤与相关性混合。</p>
     * <p><b>示例：</b></p>
     * <pre>
     * // (status = "ACTIVE") AND (score >= 80) AND title 匹配 "spring"
     * BoolQueryBuilder qb = QueryBuilderUtils.boolQuery()
     *     .must(QueryBuilderUtils.termQuery("status", "ACTIVE"))
     *     .filter(QueryBuilderUtils.rangeQuery("score").gte(80))
     *     .must(QueryBuilderUtils.matchQuery("title", "spring"));
     * </pre>
     *
     * @return {@link BoolQueryBuilder} 用于构建 bool 查询的构建器
     */
    public static BoolQueryBuilder boolQuery() {
        return QueryBuilders.boolQuery();
    }
} 