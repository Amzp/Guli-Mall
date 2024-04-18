package com.atguigu.gulimall.search.config;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: Rain^
 * @createTime: 2020-06-04 16:46
 **/

@Configuration
@Slf4j
public class GulimallElasticSearchConfig {
    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        // builder.addHeader("Authorization", "Bearer " + TOKEN);
        // builder.setHttpAsyncResponseConsumerFactory(
        //         new HttpAsyncResponseConsumerFactory
        //                 .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    /**
     * 创建并初始化RestHighLevelClient实例。
     * 该方法不接受任何参数，创建的客户端将连接到指定的Elasticsearch服务器。
     *
     * @return RestHighLevelClient 返回配置好的Elasticsearch高级REST客户端实例。
     */
    @Bean
    public RestHighLevelClient esRestClient() {
        log.info("创建Elasticsearch高级REST客户端实例");
        // 使用RestClient.builder创建RestHighLevelClient实例，配置连接到Elasticsearch的服务器地址和端口
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("114.55.178.56", 9200, "http")
                )
        );
    }


}
