package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * ClassName: ProductSaveService
 * Package: com.atguigu.gulimall.search.service
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/19 上午9:40
 * @Version 1.0
 */
public interface ProductSaveService {

    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
