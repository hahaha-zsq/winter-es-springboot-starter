# Winter Elasticsearch Spring Boot Starter

[![GitHub stars](https://img.shields.io/github/stars/hahaha-zsq/winter-es-spring-boot-starter.svg?style=social&label=Stars)](https://github.com/hahaha-zsq/winter-es-spring-boot-starter)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
![Java](https://img.shields.io/badge/Java-8%2B-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.11-brightgreen)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-7.5.2-005571)
![Lombok](https://img.shields.io/badge/Lombok-1.18.22-orange)
![Hutool%20JSON](https://img.shields.io/badge/Hutool%20JSON-5.8.25-yellow)

📘 一个基于 Spring Boot 的 Elasticsearch 自动配置 Starter，提供简洁、统一、可扩展的 ES 客户端与模板能力，开箱即用地完成文档的常见操作、查询构建与滚动搜索。

## 特性 🚀

- ✅ 多集群支持，自动连接与复用
- ✅ `EsTemplate` 提供文档增删改查与搜索 API（类似 `JdbcTemplate` 的使用体验）
- ✅ `QueryBuilderUtils` 提供常见查询构建器（match/term/range/regexp/布尔等）
- ✅ Spring Boot 自动配置，线程安全封装
- ✅ 必要日志与异常处理，便于排障

> 说明：当前版本不包含索引管理与聚合分析的模板封装，若需聚合能力请直接使用 Elasticsearch 原生 API 组装 `SearchSourceBuilder` 并在业务层自行执行。


## 安装与引入 📦

如果你从源代码本地开发使用：

```bash
# 在项目根目录执行
mvn -q -DskipTests install
```

然后在业务工程中引入依赖（以下坐标以本项目默认包名推测，具体以实际发布坐标为准）：

```xml
<dependency>
  <groupId>com.zsq</groupId>
  <artifactId>winter-es-springboot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

## 配置说明 ⚙️

`application.yml` 示例（支持多集群）：

```yaml
winter-es:
  # 是否在启动时打印 Banner（可选，默认 true）
  is-print: true
  es-configs:
    - cluster-name: default
      hosts:
        - localhost:9200
        - localhost:9201
      username: elastic
      password: password
      connect-timeout: 5000
      socket-timeout: 60000
      connection-request-timeout: 5000
    - cluster-name: secondary
      hosts:
        - es-cluster:9200
      username: admin
      password: admin123
      connect-timeout: 5000
      socket-timeout: 60000
      connection-request-timeout: 5000
```

配置项说明：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| winter-es.is-print | Boolean | true | 是否打印启动 Banner |
| winter-es.es-configs[].cluster-name | String | - | 集群名称（作为客户端键值，建议唯一且必填） |
| winter-es.es-configs[].hosts | List<String> | [] | 节点地址列表（host:port），至少 1 个，示例 `localhost:9200` |
| winter-es.es-configs[].username | String | - | 用户名（可选） |
| winter-es.es-configs[].password | String | - | 密码（可选） |
| winter-es.es-configs[].connect-timeout | Integer | 5000 | 连接超时（ms） |
| winter-es.es-configs[].socket-timeout | Integer | 60000 | Socket 超时（ms） |
| winter-es.es-configs[].connection-request-timeout | Integer | 5000 | 连接请求超时（ms） |

> 注意事项：
> - 自动装配启用条件：存在 `winter-es.es-configs` 配置项时生效。
> - 仅支持 HTTP 协议与基础认证（Basic Auth）。内部会按 `http://{host:port}` 构建连接；如需 HTTPS/证书，请自行扩展客户端。
> - `hosts` 不能为空，否则会在启动时抛出异常；`cluster-name` 用于区分与获取客户端实例，请保持唯一性。
> - 多集群时，通过业务侧传入的 `EsIndexInfo.setClusterName("...")` 指定目标集群。

## 快速开始 🏁

在业务工程中注入 `EsTemplate`：

```java
@Autowired
private EsTemplate esTemplate;
```

准备索引与文档模型：

```java
EsIndexInfo indexInfo = new EsIndexInfo()
    .setClusterName("default")
    .setIndexName("user_index");

Map<String, Object> data = new HashMap<>();
data.put("name", "张三");
data.put("age", 25);
data.put("email", "zhangsan@example.com");

EsDocData docData = new EsDocData()
    .setDocId("1")
    .setData(data);
```

### 文档 CRUD ✍️

```java
// 插入
boolean ok = esTemplate.insertDocument(indexInfo, docData);

// 更新
Map<String, Object> newData = new HashMap<>();
newData.put("age", 26);
esTemplate.updateDocument(indexInfo, new EsDocData().setDocId("1").setData(newData));

// 查询
Map<String, Object> source = esTemplate.getDocumentById(indexInfo, "1");

// 是否存在
boolean exists = esTemplate.documentExists(indexInfo, "1");

// 删除单个文档
esTemplate.deleteDocument(indexInfo, "1");

// 批量插入/更新/删除
List<EsDocData> list = new ArrayList<>();
list.add(new EsDocData().setDocId("2").setData(Collections.singletonMap("name", "李四")));
esTemplate.batchInsertDocuments(indexInfo, list);

List<String> ids = Arrays.asList("1", "2", "3");
esTemplate.batchDeleteDocuments(indexInfo, ids);

// 删除索引内全部文档（delete by query）
long deleted = esTemplate.deleteAllDocuments(indexInfo);
```

### 构建查询与搜索 🔍

`QueryBuilderUtils` 封装了常用查询（示例）：

```java
// Bool 组合： (status = "ACTIVE") AND 20 <= age < 30 AND name 全文包含 "张三"
BoolQueryBuilder bq = QueryBuilderUtils.boolQuery()
  .must(QueryBuilderUtils.termQuery("status", "ACTIVE"))
  .filter(QueryBuilderUtils.rangeQuery("age").gte(20).lt(30))
  .must(QueryBuilderUtils.matchQuery("name", "张三"));

EsSearchRequest req = new EsSearchRequest();
req.setBq(bq);
req.setFrom(0);
req.setSize(10);
req.setFields(new String[]{"name", "age", "email"});

SearchResponse resp = esTemplate.searchDocuments(indexInfo, req);
```

#### 更多查询示例 🧪

```java
// 短语匹配（词序一致）
QueryBuilder qb1 = QueryBuilderUtils.matchPhraseQuery("title", "error handling");

// 多字段全文匹配
QueryBuilder qb2 = QueryBuilderUtils.multiMatchQuery("monitoring", "title", "content");

// 精确匹配（类似 SQL '='）
QueryBuilder qb3 = QueryBuilderUtils.termQuery("status", "ACTIVE");

// IN 集合匹配
QueryBuilder qb4 = QueryBuilderUtils.termsQuery("id", 1, 2, 3);

// 正则匹配（Lucene 正则）
QueryBuilder qb5 = QueryBuilderUtils.regexpQuery("tag.keyword", "log-\\d+");
```

### 高亮 ✨

当前版本未提供高亮封装方法，可直接构造 `HighlightBuilder` 并设置到 `EsSearchRequest`：

```java
HighlightBuilder hb = new HighlightBuilder()
  .field("title")
  .field("content")
  .preTags("<em>")
  .postTags("</em>");

EsSearchRequest req = new EsSearchRequest();
req.setBq(QueryBuilderUtils.matchQuery("content", "Elasticsearch"));
req.setHighlightBuilder(hb);

SearchResponse resp = esTemplate.searchDocuments(indexInfo, req);
```

### 滚动查询（Scroll） 🔄

```java
EsSearchRequest req = new EsSearchRequest();
req.setBq(QueryBuilderUtils.boolQuery()); // 例如 matchAll 可使用 QueryBuilders.matchAllQuery()
req.setNeedScroll(true);
req.setMinutes(5L); // 滚动窗口（分钟）
req.setSize(1000);

SearchResponse resp = esTemplate.searchDocuments(indexInfo, req);
```

## 多集群 🧭

- 在 `application.yml` 中配置多个 `es-configs` 条目，通过 `EsIndexInfo.setClusterName("xxx")` 指定目标集群。
- 客户端连接由 `EsRestClient` 统一管理与复用，线程安全。

## 日志与排障 🧰

- Starter 在关键操作中输出必要日志（连接、请求与错误），可根据需要在 `application.yml` 调整日志级别：

```yaml
logging:
  level:
    com.zsq.winter.es: INFO
    org.elasticsearch.client: WARN
```

- 发生异常时，`EsTemplate` 会记录错误日志并返回安全的默认值（如 `false` 或 `null`）。建议在上层调用处增加必要的兜底与重试策略。

## 自动装配 🔧

- 基于 Spring Boot 2.x 自动配置机制，通过 `META-INF/spring.factories` 暴露 `ElasticsearchAutoConfiguration`，引入 Starter 即可生效。

## 目录结构 🗂️

```
src/
  main/
    java/com/zsq/winter/es/
      client/           # 客户端封装（EsRestClient）
      config/           # 自动配置（ElasticsearchAutoConfiguration）
      entity/           # 实体（EsIndexInfo、EsDocData、EsSearchRequest 等）
      service/          # 模板能力（EsTemplate）
      util/             # 查询构建工具（QueryBuilderUtils）
    resources/
      META-INF/spring.factories  # Spring Boot 自动配置入口
```

## 贡献 🤝

欢迎提交 Issue/PR，建议与改进都会被认真对待。提交 PR 前请通过 `mvn -DskipTests compile`，并补充必要的单元测试或示例说明。

## 许可证 📄

MIT License 