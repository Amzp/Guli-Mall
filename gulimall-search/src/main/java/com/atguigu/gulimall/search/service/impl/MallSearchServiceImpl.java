package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrResponseVo;
import com.atguigu.gulimall.search.vo.BrandVo;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: MallSearchServiceImpl
 * Package: com.atguigu.gulimall.search.service.impl
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/21 下午11:04
 * @Version 1.0
 */
@Service
@Slf4j
public class MallSearchServiceImpl implements MallSearchService {
    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result = null;

        // 1. 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            // 2. 执行检索请求
            SearchResponse response = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            // 3. 封装数据
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * 构建搜索请求
     *
     * @param param 搜索参数，包含关键字、分类ID、品牌ID、库存状态、SKU价格等筛选条件
     * @return 返回构建好的搜索请求对象
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 构建查询条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1.1 必须条件-模糊匹配关键字
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        // 1.2 过滤条件
        // 1.2.1 根据分类ID过滤
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // 1.2.2 根据品牌ID过滤
        if (param.getBrandId() != null && !param.getBrandId().isEmpty()) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 1.2.3 根据指定的属性进行过滤。
        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            for (String attrStr : param.getAttrs()) {
                // 解析属性字符串，格式为"属性ID_属性值1:属性值2:..."
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");

                // 构建查询条件，必须同时匹配属性ID和属性值
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                // 将属性查询嵌套到主查询中，以处理多值属性
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        // 1.2.4 根据库存状态过滤
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        // 1.2.5 根据SKU价格过滤
        // 根据param中的skuPrice参数构建范围查询
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_"); // 使用"_"分割skuPrice字符串

            // 如果分割后数组长度为2，则设置范围查询的最小值和最大值
            if (s.length == 2) {
                rangeQuery.gte(s[0]).lte(s[1]);
                // 如果分割后数组长度为1，根据字符串前缀和后缀判断查询范围
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]); // 小于等于指定值
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]); // 大于等于指定值
                }
            }
            boolQuery.filter(rangeQuery); // 将范围查询作为条件添加到boolQuery中
        }

        // 设置查询条件
        sourceBuilder.query(boolQuery);

        // 1.3 排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            // 按下划线分割排序参数，获取字段名和排序方式
            String[] s = sort.split("_");
            // 判断排序方式，转化为SortOrder枚举类型
            SortOrder order = "asc".equalsIgnoreCase(s[1]) ? SortOrder.ASC : SortOrder.DESC;
            // 设置查询结果的排序方式
            sourceBuilder.sort(s[0], order);
        }

        // 1.4 分页
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        // 1.5 高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            // 创建高亮构建器
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            // 设置要高亮的字段
            highlightBuilder.field("skuTitle");
            // 设置高亮标签的前缀
            highlightBuilder.preTags("<b style='color:red'>");
            // 设置高亮标签的后缀
            highlightBuilder.postTags("</b>");
            // 将高亮构建器应用到源构建器中
            sourceBuilder.highlighter(highlightBuilder);
        }


        // 2. 聚合分析
        // 这一部分代码主要进行品牌和分类信息的聚合分析，以获取各品牌和分类的统计信息。

        // 2.1 聚合分析品牌信息
        // 构建一个名为"brand_agg"的聚合操作，用于对"brandId"字段进行分组统计。
        // 设置最大返回的分组数为50，并在每个分组内进行品牌名称和品牌图片的子聚合操作。
        TermsAggregationBuilder brandAgg = AggregationBuilders
                .terms("brand_agg")
                .field("brandId") // 设置聚合字段为brandId
                .size(50) // 设置聚合结果返回的最大数量为50
                .subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1)) // 添加品牌名称的子聚合，只取一个
                .subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1)); // 添加品牌图片的子聚合，只取一个
        sourceBuilder.aggregation(brandAgg); // 将品牌聚合添加到查询中;

        // 2.2 聚合分析分类信息
        // 构建一个名为"catalog_agg"的聚合操作，用于对"catalogId"字段进行分组统计。
        // 设置最大返回的分组数为20，并在每个分组内进行子聚合操作。
        AggregationBuilder catalogAgg = AggregationBuilders
                .terms("catalog_agg")
                .field("catalogId")
                .size(20)
                // 在每个"catalogId"分组内，进一步按"catalogName"字段进行分组统计（子聚合）。
                // 设置只返回一个分组结果，主要用于获取最常见的"catalogName"。
                .subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        // 将上述聚合操作添加到查询的源数据构建器中。
        sourceBuilder.aggregation(catalogAgg);

        // 创建一个嵌套聚合分析，用于分析"attrs"字段中的属性信息
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");

        // 创建一个词项聚合，用于按属性ID分组，并添加两个子聚合：按属性名称分组和按属性值分组
        // 设置属性名称分组最多显示1个，属性值分组最多显示50个
        AggregationBuilder attrIdAgg = AggregationBuilders
                .terms("attr_id_agg").field("attrs.attrId")
                .subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1))
                .subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        // 将属性ID的聚合分析添加到嵌套聚合分析中
        attrAgg.subAggregation(attrIdAgg);

        // 将聚合分析添加到查询的源数据构建器中
        sourceBuilder.aggregation(attrAgg);


        String string = sourceBuilder.toString();
        log.debug("构建的DSL语句：{}", string);

        // 2. 构建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }


    /**
     * 构建结果数据
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {

        SearchResult result = new SearchResult();
        // 1. 返回所有查询到的商品
        // 从搜索响应中获取所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        // 检查是否有查询结果
        if (hits.getHits() != null && hits.getHits().length > 0) {
            // 遍历每一个查询到的商品
            for (SearchHit hit : hits.getHits()) {
                // 将源数据转换为SkuEsModel对象
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                // 如果有关键字，则显示高亮标题
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    // 获取高亮显示的商品标题
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String skuTitleValue = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(skuTitleValue);
                }
                // 将商品信息添加到结果列表中
                esModels.add(esModel);
            }
        }
        // 将商品列表设置到结果对象中
        result.setProducts(esModels);


        // 2. 当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        // 初始化一个空列表来存储属性信息

        // 获取属性信息的聚合结果
        ParsedNested attrsAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attr_id_agg");

        // 遍历属性ID的聚合桶，为每一个属性创建并填充AttrVo对象
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();

            // 从聚合桶中获取属性ID，并设置到AttrVo中
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            // 获取属性名，并设置到AttrVo中
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            // 获取属性的所有值，并设置到AttrVo中
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream()
                    .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                    .collect(Collectors.toList());

            attrVo.setAttrValue(attrValues);


            // 将填充好的AttrVo添加到列表中
            attrVos.add(attrVo);
        }
        // 将所有属性信息设置到搜索结果中
        result.setAttrs(attrVos);


        // 3. 当前所有商品涉及到的所有品牌信息
        // 初始化品牌信息列表
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();

        // 获取品牌聚合结果
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            // 处理每个品牌的信息
            // 1. 获取品牌ID
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);

            // 2. 获取品牌名称
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);

            // 3. 获取品牌图片
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);

            // 将品牌信息添加到列表中
            brandVos.add(brandVo);
        }
        // 设置品牌信息列表到结果对象中
        result.setBrands(brandVos);


        // 4. 当前所有商品涉及到的所有分类信息
        // 获取搜索结果中的分类聚合信息，并将其封装到CatalogVo列表中
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        // 获取名为"catalog_agg"的聚合结果
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        // 遍历聚合结果中的每个分类桶
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 从分类桶中获取分类ID，并将其设置到CatalogVo中
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            // 从分类桶中获取分类名称，并将其设置到CatalogVo中
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        // 将封装好的分类信息列表设置到搜索结果中
        result.setCatalogs(catalogVos);


        //===============以上可以从聚合信息中获取====================//


        // 5. 页码
        result.setPageNum(param.getPageNum());
        // 5.1. 总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        // 5.2. 总页码
        int totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ?
                (int) total / EsConstant.PRODUCT_PAGESIZE : ((int) total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);


        //6、构建面包屑导航
        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            List<SearchResult.NavVo> collect = param.getAttrs().stream()
                    .map(attr -> {
                        //1、分析每一个attrs传过来的参数值
                        SearchResult.NavVo navVo = new SearchResult.NavVo();
                        String[] s = attr.split("_");
                        navVo.setNavValue(s[1]);
                        R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                        result.getAttrIds().add(Long.parseLong(s[0]));
                        if (r.getCode() == 0) {
                            AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                            });
                            navVo.setNavName(data.getAttrName());
                        } else {
                            navVo.setNavName(s[0]);
                        }

                        //2、取消了这个面包屑以后，我们要跳转到哪个地方，将请求的地址url里面的当前置空
                        //拿到所有的查询条件，去掉当前
                        String replace = replaceQueryString(param, attr, "attrs");
                        navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                        return navVo;
                    }).collect(Collectors.toList());
            result.setNavs(collect);
        }
        if (param.getBrandId() != null && !param.getBrandId().isEmpty()) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            navVo.setNavName("品牌");

            // TODO 远程查询所有品牌
            R r = productFeignService.brandsInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getBrandName() + ";");
                    replace = replaceQueryString(param, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }

        // TODO 分类：不需要导航取消

        return result;
    }

    /**
     * 替换查询字符串中的特定参数值。
     *
     * @param param 搜索参数对象，包含原始查询字符串。
     * @param value 要替换的参数新值。
     * @param key   要替换的参数键。
     * @return 返回替换参数值后的查询字符串。
     */
    private static String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            // 对参数值进行URL编码
            encode = URLEncoder.encode(value, "UTF-8");
            // 替换浏览器和Java对空格编码的不同处理
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 替换查询字符串中指定参数的值并返回
        return param.get_queryString().replace("&" + key + "=" + encode, "");
    }
}
