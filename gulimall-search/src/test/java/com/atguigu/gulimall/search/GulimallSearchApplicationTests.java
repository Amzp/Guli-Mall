package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * ClassName: GulimallSearchApplicationTests
 * Package: com.atguigu.gulimall.search
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/18 下午5:29
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Resource
    private RestHighLevelClient client;


    @Data
    @Builder
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    static
    class User {
        private String userName;
        private String gender;
        private Integer age;
    }

    @Test
    public void testContextLoads() {
        long startTime = System.currentTimeMillis();
        System.out.println("testContextLoads()\n");


        // testContextLoads Code
        System.out.println(client);


        long endTime = System.currentTimeMillis();
        System.out.printf("\ntestContextLoads  Execution time: %d ms", (endTime - startTime));
    }

    /**
     * 测试索引方法
     * 本方法无参数，也无返回值，主要演示了如何使用IndexRequest创建和发送索引请求。
     */
    @Test
    public void testIndex() throws IOException {
        long startTime = System.currentTimeMillis(); // 记录方法开始执行的时间
        System.out.println("testIndex()\n");
        // 创建索引请求
        IndexRequest indexRequest = new IndexRequest("users"); // 指定索引的名称
        indexRequest.id("1"); // 设置文档的ID

        // 准备文档数据，此处使用User对象序列化为JSON字符串作为文档内容
        User user = User.builder().userName("zhangsan").gender("男").age(18).build();

        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON); // 将JSON字符串设置为文档内容

        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println("index = " + index);


        long endTime = System.currentTimeMillis(); // 记录方法执行结束的时间
        // 输出执行时间
        System.out.printf("\ntestIndex Execution time: %d ms", (endTime - startTime));
    }

    @Test
    public void testSearchData() throws IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("testSearchData()\n");


        // testSearchData Code
        // 1. 创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices("bank");
        // 指定DSL，检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchRequest.source(searchSourceBuilder);

        // 1.1 构造检索条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        System.out.println("searchSourceBuilder = " + searchSourceBuilder.toString());
        // 1.2 按照年龄的值分布聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);
        // 1.3 计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);
        System.out.println("检索条件：" + searchSourceBuilder.toString());
//        searchSourceBuilder.from();
//        searchSourceBuilder.size();

        // 2. 执行检索
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // 3. 分析结果
        System.out.println("searchResponse = " + searchResponse.toString());
        Map map = JSON.parseObject(searchResponse.toString(), Map.class);
        System.out.println("map = " + map);
        // 3.1 获取所有查到的数据
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
//        for (SearchHit hit : searchHits) {
//            String sourceAsString = hit.getSourceAsString();
//            System.out.println("sourceAsString = " + sourceAsString);
//        }
        // 3.2 获取这次检索到的分析信息
//        Aggregations aggregations = searchResponse.getAggregations();
//        for (Aggregation aggregation : aggregations.asList()) {
//            System.out.println(aggregation.getName());
//        }
//        Terms aggAgg = aggregations.get("aggAgg");
//        for (Terms.Bucket bucket : aggAgg.getBuckets()) {
//            String keyAsString = bucket.getKeyAsString();
//            System.out.println("keyAsString = " + keyAsString);
//        }


        long endTime = System.currentTimeMillis();
        System.out.printf("\ntestSearchData  Execution time: %d ms", (endTime - startTime));
    }


}
