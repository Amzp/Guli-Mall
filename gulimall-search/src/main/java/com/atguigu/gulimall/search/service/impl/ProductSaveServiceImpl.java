package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: ProductSaveServiceImpl
 * Package: com.atguigu.gulimall.search.service.impl
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/19 上午9:42
 * @Version 1.0
 */
@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {
    @Resource
    private RestHighLevelClient restHighLevelClient;


    /**
     * 将一批商品上架到Elasticsearch中。
     *
     * @param skuEsModels 包含需要上架的商品信息的列表，每个商品信息由SkuEsModel类表示。
     * @return Boolean 返回上架操作是否成功。如果操作中存在失败项，则返回true；否则返回false。
     * @throws IOException 如果执行Elasticsearch操作时发生IO异常，则抛出IOException。
     */
    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 批量保存商品信息到Elasticsearch
        // 首先，需要为Elasticsearch的商品索引设置好相应的映射关系

        // 创建批量请求对象
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            // 为每个商品创建索引请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            // 使用商品的ID作为该索引的ID
            indexRequest.id(skuEsModel.getSkuId().toString());
            // 将商品信息转换为JSON字符串，作为索引的源数据
            String jsonString = JSON.toJSONString(skuEsModel);
            indexRequest.source(jsonString, XContentType.JSON);

            // 将索引请求添加到批量请求中
            bulkRequest.add(indexRequest);
        }
        // 使用Elasticsearch客户端执行批量索引操作
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // 检查并处理批量索引操作失败的情况
        boolean hasFailures = bulk.hasFailures();
        if (hasFailures) {
            // 提取失败项的ID并收集到列表中
            List<String> collect = Arrays.stream(bulk.getItems())
                    .map(BulkItemResponse::getId)
                    .collect(Collectors.toList());
            // 记录批量保存失败的商品ID
            log.error("批量保存商品信息到Elasticsearch失败：{}", collect);
        }

        return hasFailures;

    }


}
